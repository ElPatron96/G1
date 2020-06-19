package srp;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;

import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import srp.config.DBConnectionManager;
import srp.controllers.UserController;
import srp.controllers.CustomerController;
import srp.controllers.OrderController;
import srp.controllers.ProductController;
import srp.repositories.impl.CustomerRepositoryImpl;
import srp.repositories.impl.OrderRepositoryImpl;
import srp.repositories.impl.ProductRepositoryImpl;

public class App {

    private final DBConnectionManager manager;
    private final CustomerController customerController;
    private final OrderController orderController;
    private final ProductController productController;

    public App() {
        this.manager = new DBConnectionManager();
        
        CustomerRepositoryImpl customerRepositoryImpl = new CustomerRepositoryImpl(this.manager.getDatabase());
        this.customerController = new CustomerController(customerRepositoryImpl);
        OrderRepositoryImpl orderRepositoryImpl = new OrderRepositoryImpl(this.manager.getDatabase());
        this.orderController = new OrderController(orderRepositoryImpl);
        ProductRepositoryImpl productRepositoryImpl = new ProductRepositoryImpl(this.manager.getDatabase());
        this.productController = new ProductController(productRepositoryImpl);
        
    }

    public static void main(String[] args) {
        new App().startup();
    }

    public void startup() {

        Info applicationInfo = new Info()
            .version("1.0")
            .description("Demo API");
        OpenApiOptions openApi = new OpenApiOptions(applicationInfo)
            .path("/api")
            .swagger(new SwaggerOptions("/api-ui")); // endpoint for swagger-ui
        Javalin server = Javalin.create(
            config -> {
                config.registerPlugin(new OpenApiPlugin(openApi));
            }
        ).start(7000);

        server.get("api/customer/:id", this.customerController::find);
        server.delete("api/customer/:id", this.customerController::delete);
        server.get("api/customers", this.customerController::findAll);
        server.post("api/customer", this.customerController::create);
        
        server.get("api/order/:id", this.orderController::find);
        server.get("api/orders", this.orderController::findAll);
        server.post("api/order",this.orderController::create);
        
        server.get("api/product/:id", this.productController::find);
        server.delete("api/product/:id", this.productController::delete);
        server.get("api/products", this.productController::findAll);
        server.post("api/products", this.productController::create);
        
        server.get("/hello", ctx -> ctx.html("Hello, Javalin!"));
        server.get("/users", UserController.fetchAllUsernames);
        server.get("/users/:id", UserController.fetchById);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.manager.closeDatabase();
            server.stop();
        }));

    }
}