package com.example.optiplant;

import com.example.optiplant.model.Branch;
import com.example.optiplant.model.Inventory;
import com.example.optiplant.model.Product;
import com.example.optiplant.model.Supplier;
import com.example.optiplant.model.UnitOfMeasure;
import com.example.optiplant.model.enums.Role;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.InventoryRepository;
import com.example.optiplant.repository.ProductRepository;
import com.example.optiplant.repository.SupplierRepository;
import com.example.optiplant.repository.UnitOfMeasureRepository;
import com.example.optiplant.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-test-data.sql")
class BackendIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Branch mainBranch;
    private Branch secondaryBranch;
    private Product product;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        mainBranch = branchRepository.save(branch("MAIN", "Main Branch"));
        secondaryBranch = branchRepository.save(branch("SEC", "Secondary Branch"));
        supplier = supplierRepository.save(supplier("Seed Supplier"));
        UnitOfMeasure unit = unitOfMeasureRepository.save(unit("UN", "Unit"));
        product = productRepository.save(product("SKU-001", "Seed Product", supplier, unit));
    }

    @Test
    void registerAndLoginFlowWorks() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "operator1",
                                  "fullName": "Operator One",
                                  "email": "operator1@example.com",
                                  "password": "password123",
                                  "branchId": "%s"
                                }
                                """.formatted(mainBranch.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("operator1"))
                .andReturn();

        String registerToken = readToken(registerResult);

        mockMvc.perform(get("/api/auth/verify")
                        .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("operator1@example.com"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "operator1",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("operator1"));
    }

    @Test
    void purchaseReceiveAndSaleFlowUpdateInventory() throws Exception {
        String managerToken = registerRoleAwareUser("manager1", "manager1@example.com", mainBranch.getId());
        promoteUserToManager("manager1");
        managerToken = login("manager1", "password123");

        MvcResult purchaseResult = mockMvc.perform(post("/api/purchase-orders")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderNumber": "PO-TEST-001",
                                  "supplierId": "%s",
                                  "branchId": "%s",
                                  "items": [
                                    {
                                      "productId": "%s",
                                      "quantity": 20,
                                      "unitPrice": 12.5
                                    }
                                  ]
                                }
                                """.formatted(supplier.getId(), mainBranch.getId(), product.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].lineTotal").value(250.0))
                .andReturn();

        String purchaseId = readJson(purchaseResult).get("id").asText();

        mockMvc.perform(post("/api/purchase-orders/" + purchaseId + "/receive")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        String operatorToken = registerRoleAwareUser("operator2", "operator2@example.com", mainBranch.getId());

        mockMvc.perform(post("/api/sales")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleNumber": "SALE-TEST-001",
                                  "branchId": "%s",
                                  "items": [
                                    {
                                      "productId": "%s",
                                      "quantity": 5,
                                      "unitPrice": 20
                                    }
                                  ]
                                }
                                """.formatted(mainBranch.getId(), product.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].lineTotal").value(100.0))
                .andExpect(jsonPath("$.total").value(100.0));

        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), mainBranch.getId()).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualByComparingTo("15");

        mockMvc.perform(get("/api/inventory-movements")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceType").isNotEmpty());
    }

    @Test
    void transferApproveAndCompleteMovesStockBetweenBranches() throws Exception {
        Inventory originInventory = new Inventory();
        originInventory.setProduct(product);
        originInventory.setBranch(mainBranch);
        originInventory.setQuantity(new BigDecimal("10"));
        originInventory.setReserved(BigDecimal.ZERO);
        inventoryRepository.save(originInventory);

        String managerToken = registerRoleAwareUser("manager2", "manager2@example.com", mainBranch.getId());
        promoteUserToManager("manager2");
        managerToken = login("manager2", "password123");

        MvcResult transferResult = mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transferNumber": "TRF-TEST-001",
                                  "fromBranchId": "%s",
                                  "toBranchId": "%s",
                                  "items": [
                                    {
                                      "productId": "%s",
                                      "quantity": 4
                                    }
                                  ]
                                }
                                """.formatted(mainBranch.getId(), secondaryBranch.getId(), product.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String transferId = readJson(transferResult).get("id").asText();

        mockMvc.perform(post("/api/transfers/" + transferId + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        mockMvc.perform(post("/api/transfers/" + transferId + "/complete")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        Inventory fromInventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), mainBranch.getId()).orElseThrow();
        Inventory toInventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), secondaryBranch.getId()).orElseThrow();
        assertThat(fromInventory.getQuantity()).isEqualByComparingTo("6");
        assertThat(toInventory.getQuantity()).isEqualByComparingTo("4");
    }

    private String registerRoleAwareUser(String username, String email, java.util.UUID branchId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "fullName": "Seed User",
                                  "email": "%s",
                                  "password": "password123",
                                  "branchId": "%s"
                                }
                                """.formatted(username, email, branchId)))
                .andExpect(status().isCreated())
                .andReturn();
        return readToken(result);
    }

    private void promoteUserToManager(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        user.setRole(Role.MANAGER);
        userRepository.save(user);
    }

    private String login(String usernameOrEmail, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "%s",
                                  "password": "%s"
                                }
                                """.formatted(usernameOrEmail, password)))
                .andExpect(status().isOk())
                .andReturn();
        return readToken(result);
    }

    private String readToken(MvcResult result) throws Exception {
        return readJson(result).get("token").asText();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Branch branch(String code, String name) {
        Branch branch = new Branch();
        branch.setCode(code);
        branch.setName(name);
        return branch;
    }

    private Supplier supplier(String name) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        return supplier;
    }

    private UnitOfMeasure unit(String code, String name) {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setCode(code);
        unit.setName(name);
        return unit;
    }

    private Product product(String sku, String name, Supplier supplier, UnitOfMeasure unit) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setSupplier(supplier);
        product.setUnitOfMeasure(unit);
        product.setDefaultCost(new BigDecimal("10"));
        return product;
    }
}
