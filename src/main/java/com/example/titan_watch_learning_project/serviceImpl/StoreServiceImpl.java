package com.example.titan_watch_learning_project.serviceImpl;//package com.example.titan.serviceImpl;
//
//import com.example.titan.entity.Customer;
//import com.example.titan.entity.Store;
//import com.example.titan.repository.CustomerRepository;
//import com.example.titan.repository.StoreRepository;
//import com.example.titan.service.StoreService;
import com.example.titan_watch_learning_project.entity.Customer;
import com.example.titan_watch_learning_project.entity.Store;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
import com.example.titan_watch_learning_project.repository.StoreRepository;
import com.example.titan_watch_learning_project.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepo;
    private final CustomerRepository customerRepo;

    @Override
    public String getNearestStoreInfo(Long customerId) {
        try {
            Customer customer = customerRepo.findById(customerId).orElse(null);
            if (customer != null && customer.getStoreCode() != null) {
                Store store = storeRepo.findByStoreCode(customer.getStoreCode()).orElse(null);
                if (store != null) {
                    return formatStoreInfo(store);
                }
            }
            // Fallback: first active store in DB
            return storeRepo.findAll().stream()
                    .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                    .findFirst()
                    .map(this::formatStoreInfo)
                    .orElse("📍 Visit your nearest Titan store to avail your birthday offer!");
        } catch (Exception e) {
            log.error("Error fetching store info for customer {}: {}", customerId, e.getMessage());
            return "📍 Visit your nearest Titan store!";
        }
    }

    private String formatStoreInfo(Store store) {
        return "🏪 " + store.getStoreName() + "\n" +
                "📍 " + store.getAddress() + "\n" +
                "🕐 " + store.getTimings() + "\n" +
                "📞 " + store.getPhone() + "\n" +
                "🗺️ Directions: " + store.getMapsLink();
    }
}