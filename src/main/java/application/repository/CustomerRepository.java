package application.repository;

import application.model.Customer;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryResult;

import java.util.List;

/**
 * Class is responsible for handling queries requests
 *
 * @author Oscar.Ricaud@ibm.com
 */
public class CustomerRepository {
    public List<Customer> getCustomers(Database database) {
        String query = "{" +
                "   \"selector\": {" +
                "      \"_id\": {" +
                "         \"$gt\": null" +
                "      }" +
                "   }" +
                "}";
        final QueryResult<Customer> customers = database.query(query, Customer.class);
        System.out.println("customers " + customers.getDocs());
        return customers.getDocs();
    }

    public List<Customer> getCustomerByUsername(Database database, String username) {
        String query = "{ \"selector\": { \"username\": \"" + username + "\" } }";
        System.out.println("username " + username + "  temp " + database);
        final QueryResult<Customer> customers = database.query(query, Customer.class);
        System.out.println("customers.toString" + customers.getDocs().toString());
        return customers.getDocs();
    }

    public String getCustomerById(Database database, String id) {
        String query = "{ \"selector\": { \"_id\": \"" + id + "\" } }";
        System.out.println("id " + id + "  temp " + database);
        final QueryResult<Customer> customers = database.query(query, Customer.class);
        System.out.println("customers.toString" + customers.getDocs().toString());
        return customers.getDocs().toString();
    }
}
