package com.example.optiplant.services;

import com.example.optiplant.dto.OrderItemRequest;
import com.example.optiplant.dto.PurchaseOrderRequest;
import com.example.optiplant.dto.PurchaseOrderResponse;
import com.example.optiplant.exceptions.BadRequestException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Branch;
import com.example.optiplant.model.Product;
import com.example.optiplant.model.PurchaseOrder;
import com.example.optiplant.model.PurchaseOrderItem;
import com.example.optiplant.model.Supplier;
import com.example.optiplant.model.enums.MovementType;
import com.example.optiplant.model.enums.OrderStatus;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.ProductRepository;
import com.example.optiplant.repository.PurchaseOrderRepository;
import com.example.optiplant.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderService {

    private static final DateTimeFormatter NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            InventoryService inventoryService,
            CurrentUserService currentUserService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
    }

    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderRepository.findAll().stream().map(PurchaseOrderResponse::from).toList();
    }

    public PurchaseOrderResponse findById(UUID id) {
        return PurchaseOrderResponse.from(getOrder(id));
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        currentUserService.getAuthenticatedUser();

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new NotFoundException("Supplier not found"));
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));
        String orderNumber = request.orderNumber() == null || request.orderNumber().isBlank()
                ? "PO-" + LocalDateTime.now().format(NUMBER_FORMAT)
                : request.orderNumber().trim();

        if (purchaseOrderRepository.existsByOrderNumber(orderNumber)) {
            throw new BadRequestException("Purchase order number is already registered");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber(orderNumber);
        order.setSupplier(supplier);
        order.setBranch(branch);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            order.getItems().add(item);

            BigDecimal unitPrice = itemRequest.unitPrice() == null ? BigDecimal.ZERO : itemRequest.unitPrice();
            total = total.add(unitPrice.multiply(itemRequest.quantity()));
        }

        order.setTotal(total);
        return PurchaseOrderResponse.from(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse receive(UUID id) {
        currentUserService.getAuthenticatedUser();
        PurchaseOrder order = getOrder(id);

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new BadRequestException("Purchase order has already been received");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled purchase orders cannot be received");
        }

        for (PurchaseOrderItem item : order.getItems()) {
            inventoryService.increaseStock(
                    item.getProduct().getId(),
                    order.getBranch().getId(),
                    item.getQuantity(),
                    MovementType.PURCHASE_IN,
                    order.getOrderNumber(),
                    "Purchase order received",
                    "PURCHASE_ORDER",
                    order.getId().toString()
            );
        }

        order.setStatus(OrderStatus.RECEIVED);
        return PurchaseOrderResponse.from(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse cancel(UUID id) {
        currentUserService.getAuthenticatedUser();
        PurchaseOrder order = getOrder(id);

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new BadRequestException("Received purchase orders cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Purchase order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return PurchaseOrderResponse.from(purchaseOrderRepository.save(order));
    }

    private PurchaseOrder getOrder(UUID id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));
    }
}
