package application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import application.model.Customer;
import junit.framework.Assert;

/**
 * The purpose of this class is to test the @see CustomerController class rest end points.
 * @author Oscar I. Ricaud
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CustomerControllerTest {
    private HttpHeaders headers = new HttpHeaders();
    Random rnd = new Random();
    int number = rnd.nextInt(9999);

    // this will convert any number sequence into 6 character.
    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    private Customer customer = new Customer(String.format("%06d", number), null, "yooyo",
            "passw0rd", "josh", "hernandez", "helloworld@gmail.com",
            "test.png");

    private SecurityContext ctx = SecurityContextHolder.createEmptyContext();

    @Before
    public void constructor() {
        // Define security context
        SecurityContextHolder.setContext(ctx);
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", "", Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    }

    /**
     * Method is responsible for checking the /check rest end point
     *
     * @throws URISyntaxException
     */
    @Test
    public void check() throws URISyntaxException {
        ResponseEntity<String> entity = this.restTemplate.getForEntity("/customer/check", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).contains("It works!");
    }

        /**
     * Method is responsible for testing the functionality of adding a customer to the database
     * table
     *
     * @throws URISyntaxException
     */
    @Test
    public void addCustomer() throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Customer> request = new HttpEntity<>(customer, headers);
        ResponseEntity<Customer> result = this.restTemplate.postForEntity("/customer/add", request, Customer.class); 
        
        // Verify request succeed
        Assert.assertEquals(201, result.getStatusCodeValue());
    }
}
