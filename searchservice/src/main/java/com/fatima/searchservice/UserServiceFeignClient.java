package com.fatima.searchservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "userservice", url = "${userservice.url}", configuration = FeignConfig.class)
public interface UserServiceFeignClient {
    
    @GetMapping("/users/search/{username}")
    List<User> getUsersByUsername(@PathVariable("username") String username);

    @GetMapping("/users/user")
    User getOneUsersByUsername(@RequestParam("username") String username);
}
