package com.example.optiplant.services;

import com.example.optiplant.dto.BranchResponse;
import com.example.optiplant.dto.InventoryMovementResponse;
import com.example.optiplant.dto.InventoryResponse;
import com.example.optiplant.dto.ProductResponse;
import com.example.optiplant.dto.PurchaseOrderResponse;
import com.example.optiplant.dto.RealtimeEvent;
import com.example.optiplant.dto.SaleResponse;
import com.example.optiplant.dto.TransferResponse;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeNotificationService {

    public static final String INVENTORY_TOPIC = "/topic/inventory";
    public static final String INVENTORY_MOVEMENTS_TOPIC = "/topic/inventory-movements";
    public static final String TRANSFERS_TOPIC = "/topic/transfers";
    public static final String PURCHASE_ORDERS_TOPIC = "/topic/purchase-orders";
    public static final String PRODUCTS_TOPIC = "/topic/products";
    public static final String BRANCHES_TOPIC = "/topic/branches";
    public static final String SALES_TOPIC = "/topic/sales";

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void inventoryUpdated(InventoryResponse inventory) {
        send(INVENTORY_TOPIC, inventory);
    }

    public void inventoryMovementCreated(InventoryMovementResponse movement) {
        send(INVENTORY_MOVEMENTS_TOPIC, RealtimeEvent.created(movement.id(), movement));
    }

    public void transferUpdated(TransferResponse transfer) {
        send(TRANSFERS_TOPIC, transfer);
    }

    public void purchaseOrderCreated(PurchaseOrderResponse purchaseOrder) {
        send(PURCHASE_ORDERS_TOPIC, RealtimeEvent.created(purchaseOrder.id(), purchaseOrder));
    }

    public void purchaseOrderUpdated(PurchaseOrderResponse purchaseOrder) {
        send(PURCHASE_ORDERS_TOPIC, RealtimeEvent.updated(purchaseOrder.id(), purchaseOrder));
    }

    public void productCreated(ProductResponse product) {
        send(PRODUCTS_TOPIC, RealtimeEvent.created(product.id(), product));
    }

    public void productUpdated(ProductResponse product) {
        send(PRODUCTS_TOPIC, RealtimeEvent.updated(product.id(), product));
    }

    public void productDeleted(UUID id) {
        send(PRODUCTS_TOPIC, RealtimeEvent.deleted(id));
    }

    public void branchCreated(BranchResponse branch) {
        send(BRANCHES_TOPIC, RealtimeEvent.created(branch.id(), branch));
    }

    public void branchUpdated(BranchResponse branch) {
        send(BRANCHES_TOPIC, RealtimeEvent.updated(branch.id(), branch));
    }

    public void branchDeleted(UUID id) {
        send(BRANCHES_TOPIC, RealtimeEvent.deleted(id));
    }

    public void saleCreated(SaleResponse sale) {
        send(SALES_TOPIC, RealtimeEvent.created(sale.id(), sale));
    }

    public void saleUpdated(SaleResponse sale) {
        send(SALES_TOPIC, RealtimeEvent.updated(sale.id(), sale));
    }

    private void send(String topic, Object payload) {
        try {
            messagingTemplate.convertAndSend(topic, payload);
        } catch (Exception ignored) {
            // Realtime delivery should not fail the committed REST operation.
        }
    }
}
