package application.controller;

import application.config.CloudantProperties;
import application.model.Customer;
import application.repository.CustomerRepository;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Class is responsible for handling rest end points
 */
@RestController
@RequestMapping("/customer")
@Api(value = "Customer Management System")
public class CustomerController {
    final private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
    final private CustomerRepository customerRepository = new CustomerRepository();
    final private CloudantProperties cloudantProperties;

    public CustomerController(CloudantProperties cloudantProperties) {
        this.cloudantProperties = cloudantProperties;
    }

    /**
     * check
     */
    @RequestMapping("/check")
    @ResponseBody
    public ResponseEntity<String> check() {
        // test the cloudant connection
        try {
            cloudantProperties.getCloudantDatabase().info();
            return ResponseEntity.ok("It works!");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Add customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Add a customer")
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> addCustomer(@RequestHeader Map<String, String> headers, @RequestBody Customer payload) {
        System.out.println("this is being run");
        try {
            // TODO: no one should have access to do this, it's not exposed to APIC
            final Database cloudant = cloudantProperties.getCloudantDatabase();

            if (payload.get_id() != null && cloudant.contains(payload.get_id())) {
                return ResponseEntity.badRequest().body("Id " + payload.get_id() + " already exists");
            }

            String customer = customerRepository.getCustomerByUsername(cloudantProperties.getCloudantDatabase(), payload.getUsername()).toString();
            if (Strings.isNullOrEmpty(customer)) {
                return ResponseEntity.badRequest().body("Customer with name " + payload.getUsername() + " already exists");
            }

            final Response resp = cloudant.save(payload);
            if (resp.getError() == null) {
                // HTTP 201 CREATED
                final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(resp.getId()).toUri();
                return ResponseEntity.created(location).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp.getError());
            }

        } catch (Exception ex) {
            logger.error("Error creating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating customer: " + ex.toString());
        }

    }

    /**
     * Update customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Update customer by id")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, consumes = "application/json")
    protected ResponseEntity<?>
    updateCustomerById(@RequestHeader Map<String, String> isAuthenticated, @PathVariable String id, @RequestBody Customer payload) {
        try {
            //final String customerId = customerRepository.getCustomerId();
            if (isAuthenticated == null) {
                // if no user passed in, this is a bad request
                return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
            }
            if (isAuthenticated.get("securitycontext").equals("false")) {
                return ResponseEntity.badRequest().body("User does not have enough access to make such query");
            }

            logger.info("caller: " + payload.get_id());
            if (!payload.get_id().equals(id)) {
                // if i'm getting a customer ID that doesn't match my own ID, then return 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final Database cloudant = cloudantProperties.getCloudantDatabase();

            // Find the customer with the old values
            Customer customer = cloudantProperties.getCloudantDatabase().find(Customer.class, id);

            // _rev is set to null from the test case, get the _rev and set it to the payload
            payload.set_rev(customer.get_rev());

            // set the payload to be the customer
            customer = payload;

            // update the database
            cloudant.update(customer);

        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error updating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating customer: " + ex.toString());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * @return customer by username
     */
    @PreAuthorize("#oauth2.hasScope('admin')")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    protected @ResponseBody
    ResponseEntity<?> searchCustomers(@RequestHeader Map<String, String> headers, @RequestParam(required = true) String username) {
        try {
            if (username == null) {
                return ResponseEntity.badRequest().body("Missing username");
            }
            Customer customer = customerRepository.getCustomerByUsername(cloudantProperties.getCloudantDatabase(), username).get(0);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * @return customer by username
     */
    @ApiOperation(value = "Search a customer by id", response = Customer.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    protected @ResponseBody
    ResponseEntity<?> searchCustomerById(@PathVariable("id") String id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body("Missing username");
            }
            return ResponseEntity.ok(customerRepository.getCustomerById(cloudantProperties.getCloudantDatabase(), id));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Delete a customer by id")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    protected ResponseEntity<?> deleteCustomerById(@PathVariable String id) {
        // TODO: no one should have access to do this, it's not exposed to APIC
        try {
            final Database cloudant = cloudantProperties.getCloudantDatabase();
            final Customer cust = cloudantProperties.getCloudantDatabase().find(Customer.class, id);
            cloudant.remove(cust);
        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error deleting customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting customer: " + ex.toString());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * @return all customers
     * @throws Exception
     */

    @ApiOperation(value = "View a list of available customers", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    }
    )
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    protected ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.getCustomers(cloudantProperties.getCloudantDatabase()));
    }

}
