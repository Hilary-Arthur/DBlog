package com.example.dblog;

import com.example.dblog.entity.BasicInfo;
import com.example.dblog.entity.User;
import com.example.dblog.repository.BasicInfoRepository;
import com.example.dblog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final BasicInfoRepository basicInfoRepo;

    public DataInitializer(UserRepository userRepo, BasicInfoRepository basicInfoRepo) {
        this.userRepo = userRepo;
        this.basicInfoRepo = basicInfoRepo;
    }

    @Override
    public void run(String... args) {
        syncBasicInfo();
    }

    private void syncBasicInfo() {
        List<User> users = userRepo.findAll();
        for (User u : users) {
            if (!basicInfoRepo.existsById(u.getUid())) {
                basicInfoRepo.save(new BasicInfo(u));
            }
        }
    }
}
