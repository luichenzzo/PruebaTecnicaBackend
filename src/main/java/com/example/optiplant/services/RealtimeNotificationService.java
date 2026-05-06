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

/**
 * Publishes domain changes to WebSocket topics for realtime clients.
 */
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

    /**
     * Publishes an inventory update event.
     *
     * @param inventory updated inventory payload
     */
    public void inventoryUpdated(InventoryResponse inventory) {
        send(INVENTORY_TOPIC, inventory);
    }

    /**
     * Publishes an inventory movement creation event.
     *
     * @param movement created movement payload
     */
    public void inventoryMovementCreated(InventoryMovementResponse movement) {
        send(INVENTORY_MOVEMENTS_TOPIC, RealtimeEvent.created(movement.id(), movement));
    }

    /**
     * Publishes a transfer update event.
     *
     * @param transfer updated transfer payload
     */
    public void transferUpdated(TransferResponse transfer) {
        send(TRANSFERS_TOPIC, transfer);
    }

    /**
     * Publishes a purchase order creation event.
     *
     * @param purchaseOrder created purchase order payload
     */
    public void purchaseOrderCreated(PurchaseOrderResponse purchaseOrder) {
        send(PURCHASE_ORDERS_TOPIC, RealtimeEvent.created(purchaseOrder.id(), purchaseOrder));
    }

    /**
     * Publishes a purchase order update event.
     *
     * @param purchaseOrder updated purchase order payload
     */
    public void purchaseOrderUpdated(PurchaseOrderResponse purchaseOrder) {
        send(PURCHASE_ORDERS_TOPIC, RealtimeEvent.updated(purchaseOrder.id(), purchaseOrder));
    }

    /**
     * Publishes a product creation event.
     *
     * @param product created product payload
     */
    public void productCreated(ProductResponse product) {
        send(PRODUCTS_TOPIC, RealtimeEvent.created(product.id(), product));
    }

    /**
     * Publishes a product update event.
     *
     * @param product updated product payload
     */
    public void productUpdated(ProductResponse product) {
        send(PRODUCTS_TOPIC, RealtimeEvent.updated(product.id(), product));
    }

    /**
     * Publishes a product deletion event.
     *
     * @param id deleted product identifier
     */
    public void productDeleted(UUID id) {
        send(PRODUCTS_TOPIC, RealtimeEvent.deleted(id));
    }

    /**
     * Publishes a branch creation event.
     *
     * @param branch created branch payload
     */
    public void branchCreated(BranchResponse branch) {
        send(BRANCHES_TOPIC, RealtimeEvent.created(branch.id(), branch));
    }

    /**
     * Publishes a branch update event.
     *
     * @param branch updated branch payload
     */
    public void branchUpdated(BranchResponse branch) {
        send(BRANCHES_TOPIC, RealtimeEvent.updated(branch.id(), branch));
    }

    /**
     * Publishes a branch deletion event.
     *
     * @param id deleted branch identifier
     */
    public void branchDeleted(UUID id) {
        send(BRANCHES_TOPIC, RealtimeEvent.deleted(id));
    }

    /**
     * Publishes a sale creation event.
     *
     * @param sale created sale payload
     */
    public void saleCreated(SaleResponse sale) {
        send(SALES_TOPIC, RealtimeEvent.created(sale.id(), sale));
    }

    /**
     * Publishes a sale update event.
     *
     * @param sale updated sale payload
     */
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
