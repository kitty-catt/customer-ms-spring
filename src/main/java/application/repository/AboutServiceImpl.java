package application.repository;

import application.model.About;
import org.springframework.stereotype.Service;

@Service
public class AboutServiceImpl implements AboutService {

    @Override
    public About getInfo() {
        // TODO Auto-generated method stub
        return new About("Customer Service", "Storefront", "Manages all customer data");
    }

}
