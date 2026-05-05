package com.example.optiplant.services;

import com.example.optiplant.dto.OrderItemRequest;
import com.example.optiplant.dto.SaleRequest;
import com.example.optiplant.dto.SaleResponse;
import com.example.optiplant.exceptions.BadRequestException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Branch;
import com.example.optiplant.model.Product;
import com.example.optiplant.model.Sale;
import com.example.optiplant.model.SaleItem;
import com.example.optiplant.model.enums.MovementType;
import com.example.optiplant.model.enums.SaleStatus;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.ProductRepository;
import com.example.optiplant.repository.SaleRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

    private static final DateTimeFormatter NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final SaleRepository saleRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final InventoryService inventoryService;
    private final RealtimeNotificationService realtimeNotificationService;

    public SaleService(
            SaleRepository saleRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService,
            InventoryService inventoryService,
            RealtimeNotificationService realtimeNotificationService
    ) {
        this.saleRepository = saleRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
        this.inventoryService = inventoryService;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream().map(SaleResponse::from).toList();
    }

    public List<SaleResponse> findAll(UUID branchId) {
        if (branchId == null) {
            return findAll();
        }
        return saleRepository.findByBranchId(branchId).stream().map(SaleResponse::from).toList();
    }

    public SaleResponse findById(UUID id) {
        return SaleResponse.from(getSale(id));
    }

    @Transactional
    public SaleResponse create(SaleRequest request) {
        currentUserService.getAuthenticatedUser();

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));
        String saleNumber = request.saleNumber() == null || request.saleNumber().isBlank()
                ? "SALE-" + LocalDateTime.now().format(NUMBER_FORMAT)
                : request.saleNumber().trim();

        if (saleRepository.existsBySaleNumber(saleNumber)) {
            throw new BadRequestException("Sale number is already registered");
        }

        Sale sale = new Sale();
        sale.setSaleNumber(saleNumber);
        sale.setBranch(branch);
        sale.setStatus(SaleStatus.COMPLETED);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemRequest.quantity());
            saleItem.setUnitPrice(itemRequest.unitPrice());
            sale.getItems().add(saleItem);

            BigDecimal unitPrice = itemRequest.unitPrice() == null ? BigDecimal.ZERO : itemRequest.unitPrice();
            total = total.add(unitPrice.multiply(itemRequest.quantity()));
            inventoryService.decreaseStock(
                    product.getId(),
                    branch.getId(),
                    itemRequest.quantity(),
                    MovementType.SALE_OUT,
                    saleNumber,
                    "Sale registered",
                    "SALE",
                    saleNumber
            );
        }

        sale.setTotal(total);
        SaleResponse response = SaleResponse.from(saleRepository.save(sale));
        realtimeNotificationService.saleCreated(response);
        return response;
    }

    private Sale getSale(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found"));
    }

    @Transactional
    public SaleResponse cancel(UUID id) {
        currentUserService.getAuthenticatedUser();
        Sale sale = getSale(id);

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BadRequestException("Sale is already cancelled");
        }

        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new BadRequestException("Only completed sales can be cancelled");
        }

        // Reverse inventory for each item (restore stock)
        for (SaleItem item : sale.getItems()) {
            inventoryService.increaseStock(
                    item.getProduct().getId(),
                    sale.getBranch().getId(),
                    item.getQuantity(),
                    MovementType.ADJUSTMENT,
                    sale.getSaleNumber(),
                    "Sale cancelled",
                    "SALE",
                    sale.getId().toString()
            );
        }

        sale.setStatus(SaleStatus.CANCELLED);
        SaleResponse response = SaleResponse.from(saleRepository.save(sale));
        realtimeNotificationService.saleUpdated(response);
        return response;
    }
}
