package com.example.optiplant.services;

import com.example.optiplant.dto.BranchResponse;
import com.example.optiplant.dto.InventoryMovementResponse;
import com.example.optiplant.dto.InventoryResponse;
import com.example.optiplant.dto.ProductResponse;
import com.example.optiplant.dto.PurchaseOrderResponse;
import com.example.optiplant.dto.RealtimeEvent;
import com.example.optiplant.dto.SaleResponse;
import com.example.optiplant.dto.TransferResponse;
import com.example.optiplant.model.enums.MovementType;
import com.example.optiplant.model.enums.OrderStatus;
import com.example.optiplant.model.enums.SaleStatus;
import com.example.optiplant.model.enums.TransferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RealtimeNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RealtimeNotificationService realtimeNotificationService;

    @Test
    void exposesExactlyTheFrontendRealtimeTopicContract() {
        assertThat(List.of(
                RealtimeNotificationService.INVENTORY_TOPIC,
                RealtimeNotificationService.INVENTORY_MOVEMENTS_TOPIC,
                RealtimeNotificationService.TRANSFERS_TOPIC,
                RealtimeNotificationService.PURCHASE_ORDERS_TOPIC,
                RealtimeNotificationService.PRODUCTS_TOPIC,
                RealtimeNotificationService.BRANCHES_TOPIC,
                RealtimeNotificationService.SALES_TOPIC
        )).containsExactly(
                "/topic/inventory",
                "/topic/inventory-movements",
                "/topic/transfers",
                "/topic/purchase-orders",
                "/topic/products",
                "/topic/branches",
                "/topic/sales"
        ).doesNotContain("/topic/movements");
    }

    @Test
    void realtimeEventCreatedFactoryUsesCreatedActionAndKeepsPayloadInstance() {
        ProductResponse product = productResponse(UUID.randomUUID());

        RealtimeEvent<ProductResponse> event = RealtimeEvent.created(product.id(), product);

        assertThat(event.action()).isEqualTo("CREATED");
        assertThat(event.id()).isEqualTo(product.id());
        assertThat(event.data()).isSameAs(product);
    }

    @Test
    void realtimeEventUpdatedFactoryUsesUpdatedActionAndKeepsPayloadInstance() {
        BranchResponse branch = branchResponse(UUID.randomUUID());

        RealtimeEvent<BranchResponse> event = RealtimeEvent.updated(branch.id(), branch);

        assertThat(event.action()).isEqualTo("UPDATED");
        assertThat(event.id()).isEqualTo(branch.id());
        assertThat(event.data()).isSameAs(branch);
    }

    @Test
    void realtimeEventDeletedFactoryUsesDeletedActionAndNullPayload() {
        UUID id = UUID.randomUUID();

        RealtimeEvent<Object> event = RealtimeEvent.deleted(id);

        assertThat(event.action()).isEqualTo("DELETED");
        assertThat(event.id()).isEqualTo(id);
        assertThat(event.data()).isNull();
    }

    @Test
    void inventoryUpdatesKeepRawDtoPayloadForBackwardCompatibility() {
        InventoryResponse inventory = inventoryResponse(UUID.randomUUID());

        realtimeNotificationService.inventoryUpdated(inventory);

        Object payload = capturePayload(RealtimeNotificationService.INVENTORY_TOPIC);
        assertThat(payload).isSameAs(inventory);
        assertThat(payload).isNotInstanceOf(RealtimeEvent.class);
    }

    @Test
    void transferUpdatesKeepRawDtoPayloadForBackwardCompatibility() {
        TransferResponse transfer = transferResponse(UUID.randomUUID());

        realtimeNotificationService.transferUpdated(transfer);

        Object payload = capturePayload(RealtimeNotificationService.TRANSFERS_TOPIC);
        assertThat(payload).isSameAs(transfer);
        assertThat(payload).isNotInstanceOf(RealtimeEvent.class);
    }

    @Test
    void inventoryMovementCreatedPublishesCreatedEventOnInventoryMovementsTopic() {
        InventoryMovementResponse movement = inventoryMovementResponse(UUID.randomUUID());

        realtimeNotificationService.inventoryMovementCreated(movement);

        assertEvent(
                RealtimeNotificationService.INVENTORY_MOVEMENTS_TOPIC,
                "CREATED",
                movement.id(),
                movement
        );
    }

    @Test
    void purchaseOrderCreatedPublishesCreatedEventOnPurchaseOrdersTopic() {
        PurchaseOrderResponse purchaseOrder = purchaseOrderResponse(UUID.randomUUID(), OrderStatus.PENDING);

        realtimeNotificationService.purchaseOrderCreated(purchaseOrder);

        assertEvent(
                RealtimeNotificationService.PURCHASE_ORDERS_TOPIC,
                "CREATED",
                purchaseOrder.id(),
                purchaseOrder
        );
    }

    @Test
    void purchaseOrderUpdatedPublishesUpdatedEventOnPurchaseOrdersTopic() {
        PurchaseOrderResponse purchaseOrder = purchaseOrderResponse(UUID.randomUUID(), OrderStatus.RECEIVED);

        realtimeNotificationService.purchaseOrderUpdated(purchaseOrder);

        assertEvent(
                RealtimeNotificationService.PURCHASE_ORDERS_TOPIC,
                "UPDATED",
                purchaseOrder.id(),
                purchaseOrder
        );
    }

    @Test
    void productCreatedPublishesCreatedEventOnProductsTopic() {
        ProductResponse product = productResponse(UUID.randomUUID());

        realtimeNotificationService.productCreated(product);

        assertEvent(
                RealtimeNotificationService.PRODUCTS_TOPIC,
                "CREATED",
                product.id(),
                product
        );
    }

    @Test
    void productUpdatedPublishesUpdatedEventOnProductsTopic() {
        ProductResponse product = productResponse(UUID.randomUUID());

        realtimeNotificationService.productUpdated(product);

        assertEvent(
                RealtimeNotificationService.PRODUCTS_TOPIC,
                "UPDATED",
                product.id(),
                product
        );
    }

    @Test
    void productDeletedPublishesDeletedEventOnProductsTopic() {
        UUID productId = UUID.randomUUID();

        realtimeNotificationService.productDeleted(productId);

        assertDeletedEvent(RealtimeNotificationService.PRODUCTS_TOPIC, productId);
    }

    @Test
    void branchCreatedPublishesCreatedEventOnBranchesTopic() {
        BranchResponse branch = branchResponse(UUID.randomUUID());

        realtimeNotificationService.branchCreated(branch);

        assertEvent(
                RealtimeNotificationService.BRANCHES_TOPIC,
                "CREATED",
                branch.id(),
                branch
        );
    }

    @Test
    void branchUpdatedPublishesUpdatedEventOnBranchesTopic() {
        BranchResponse branch = branchResponse(UUID.randomUUID());

        realtimeNotificationService.branchUpdated(branch);

        assertEvent(
                RealtimeNotificationService.BRANCHES_TOPIC,
                "UPDATED",
                branch.id(),
                branch
        );
    }

    @Test
    void branchDeletedPublishesDeletedEventOnBranchesTopic() {
        UUID branchId = UUID.randomUUID();

        realtimeNotificationService.branchDeleted(branchId);

        assertDeletedEvent(RealtimeNotificationService.BRANCHES_TOPIC, branchId);
    }

    @Test
    void saleCreatedPublishesCreatedEventOnSalesTopic() {
        SaleResponse sale = saleResponse(UUID.randomUUID(), SaleStatus.COMPLETED);

        realtimeNotificationService.saleCreated(sale);

        assertEvent(
                RealtimeNotificationService.SALES_TOPIC,
                "CREATED",
                sale.id(),
                sale
        );
    }

    @Test
    void saleUpdatedPublishesUpdatedEventOnSalesTopic() {
        SaleResponse sale = saleResponse(UUID.randomUUID(), SaleStatus.CANCELLED);

        realtimeNotificationService.saleUpdated(sale);

        assertEvent(
                RealtimeNotificationService.SALES_TOPIC,
                "UPDATED",
                sale.id(),
                sale
        );
    }

    @Test
    void createdEventsNeverUseDeleteSemantics() {
        ProductResponse product = productResponse(UUID.randomUUID());

        realtimeNotificationService.productCreated(product);

        RealtimeEvent<?> event = captureEvent(RealtimeNotificationService.PRODUCTS_TOPIC);
        assertThat(event.action()).isNotEqualTo("DELETED");
        assertThat(event.data()).isNotNull();
    }

    @Test
    void updatedEventsNeverUseCreateSemantics() {
        BranchResponse branch = branchResponse(UUID.randomUUID());

        realtimeNotificationService.branchUpdated(branch);

        RealtimeEvent<?> event = captureEvent(RealtimeNotificationService.BRANCHES_TOPIC);
        assertThat(event.action()).isNotEqualTo("CREATED");
        assertThat(event.data()).isSameAs(branch);
    }

    @Test
    void deletedProductEventDoesNotLeakStaleEntityData() {
        UUID productId = UUID.randomUUID();

        realtimeNotificationService.productDeleted(productId);

        assertThat(captureEvent(RealtimeNotificationService.PRODUCTS_TOPIC).data()).isNull();
    }

    @Test
    void deletedBranchEventDoesNotLeakStaleEntityData() {
        UUID branchId = UUID.randomUUID();

        realtimeNotificationService.branchDeleted(branchId);

        assertThat(captureEvent(RealtimeNotificationService.BRANCHES_TOPIC).data()).isNull();
    }

    @Test
    void inventorySendFailureIsSwallowedSoBusinessTransactionCanContinue() {
        InventoryResponse inventory = inventoryResponse(UUID.randomUUID());
        doThrow(new RuntimeException("broker unavailable"))
                .when(messagingTemplate)
                .convertAndSend(anyString(), any(Object.class));

        assertThatNoException().isThrownBy(() -> realtimeNotificationService.inventoryUpdated(inventory));

        verify(messagingTemplate).convertAndSend(RealtimeNotificationService.INVENTORY_TOPIC, inventory);
    }

    @Test
    void eventEnvelopeSendFailureIsSwallowedSoBusinessTransactionCanContinue() {
        PurchaseOrderResponse purchaseOrder = purchaseOrderResponse(UUID.randomUUID(), OrderStatus.CANCELLED);
        doThrow(new RuntimeException("broker unavailable"))
                .when(messagingTemplate)
                .convertAndSend(anyString(), any(Object.class));

        assertThatNoException().isThrownBy(() -> realtimeNotificationService.purchaseOrderUpdated(purchaseOrder));

        verify(messagingTemplate).convertAndSend(eq(RealtimeNotificationService.PURCHASE_ORDERS_TOPIC), any(Object.class));
    }

    @Test
    void inventoryMovementEventPreservesMovementMetadataForFrontendFiltering() {
        UUID movementId = UUID.randomUUID();
        InventoryMovementResponse movement = inventoryMovementResponse(movementId);

        realtimeNotificationService.inventoryMovementCreated(movement);

        RealtimeEvent<?> event = captureEvent(RealtimeNotificationService.INVENTORY_MOVEMENTS_TOPIC);
        assertThat(event.data()).isSameAs(movement);
        assertThat(((InventoryMovementResponse) event.data()).movementCategory()).isEqualTo("IN");
        assertThat(((InventoryMovementResponse) event.data()).movementType()).isEqualTo(MovementType.PURCHASE_IN);
        assertThat(((InventoryMovementResponse) event.data()).sourceType()).isEqualTo("PURCHASE_ORDER");
    }

    @Test
    void purchaseOrderEventPreservesStatusForFrontendUpsert() {
        PurchaseOrderResponse purchaseOrder = purchaseOrderResponse(UUID.randomUUID(), OrderStatus.CANCELLED);

        realtimeNotificationService.purchaseOrderUpdated(purchaseOrder);

        RealtimeEvent<?> event = captureEvent(RealtimeNotificationService.PURCHASE_ORDERS_TOPIC);
        assertThat(((PurchaseOrderResponse) event.data()).status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void saleEventPreservesStatusForFrontendUpsert() {
        SaleResponse sale = saleResponse(UUID.randomUUID(), SaleStatus.CANCELLED);

        realtimeNotificationService.saleUpdated(sale);

        RealtimeEvent<?> event = captureEvent(RealtimeNotificationService.SALES_TOPIC);
        assertThat(((SaleResponse) event.data()).status()).isEqualTo(SaleStatus.CANCELLED);
    }

    private void assertEvent(String topic, String action, UUID id, Object data) {
        RealtimeEvent<?> event = captureEvent(topic);
        assertThat(event.action()).isEqualTo(action);
        assertThat(event.id()).isEqualTo(id);
        assertThat(event.data()).isSameAs(data);
    }

    private void assertDeletedEvent(String topic, UUID id) {
        RealtimeEvent<?> event = captureEvent(topic);
        assertThat(event.action()).isEqualTo("DELETED");
        assertThat(event.id()).isEqualTo(id);
        assertThat(event.data()).isNull();
    }

    private Object capturePayload(String topic) {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq(topic), payloadCaptor.capture());
        return payloadCaptor.getValue();
    }

    private RealtimeEvent<?> captureEvent(String topic) {
        Object payload = capturePayload(topic);
        assertThat(payload).isInstanceOf(RealtimeEvent.class);
        return (RealtimeEvent<?>) payload;
    }

    private InventoryResponse inventoryResponse(UUID id) {
        return new InventoryResponse(
                id,
                UUID.randomUUID(),
                "SKU-001",
                "Seed Product",
                UUID.randomUUID(),
                new BigDecimal("12"),
                BigDecimal.ZERO,
                UUID.randomUUID(),
                null
        );
    }

    private InventoryMovementResponse inventoryMovementResponse(UUID id) {
        return new InventoryMovementResponse(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-001",
                UUID.randomUUID(),
                "IN",
                MovementType.PURCHASE_IN,
                new BigDecimal("5"),
                "PO-001",
                "Received",
                "PURCHASE_ORDER",
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 5, 5, 10, 30),
                UUID.randomUUID()
        );
    }

    private TransferResponse transferResponse(UUID id) {
        return new TransferResponse(
                id,
                "TRF-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                TransferStatus.PENDING,
                List.of(),
                UUID.randomUUID(),
                null
        );
    }

    private PurchaseOrderResponse purchaseOrderResponse(UUID id, OrderStatus status) {
        return new PurchaseOrderResponse(
                id,
                "PO-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                new BigDecimal("100"),
                List.of(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    private ProductResponse productResponse(UUID id) {
        return new ProductResponse(
                id,
                "SKU-001",
                "Seed Product",
                "Ready to plant",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("10"),
                UUID.randomUUID(),
                null
        );
    }

    private BranchResponse branchResponse(UUID id) {
        return new BranchResponse(
                id,
                "MAIN",
                "Main Branch",
                "Main Street",
                UUID.randomUUID(),
                null
        );
    }

    private SaleResponse saleResponse(UUID id, SaleStatus status) {
        return new SaleResponse(
                id,
                "SALE-001",
                UUID.randomUUID(),
                status,
                new BigDecimal("80"),
                List.of(),
                UUID.randomUUID(),
                null
        );
    }
}
