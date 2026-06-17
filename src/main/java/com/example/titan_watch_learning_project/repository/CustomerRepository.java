package com.example.titan_watch_learning_project.repository;//package com.example.titan.repository;
//import com.example.titan.entity.Customer;
import com.example.titan_watch_learning_project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    List<Customer> findByBirthDayAndBirthMonth(Integer birthDay, Integer birthMonth);
}