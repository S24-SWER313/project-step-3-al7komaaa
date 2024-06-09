package com.fatima.searchservice;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/search")
public class searchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/search/{username}")
    public ResponseEntity<List<User>> getUsersByUsername(@PathVariable("username") String username) {
        List<User> users = searchService.searchUsersByUsername(username);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search/user")
    public ResponseEntity<User> getOneUserByUsername(@RequestParam("username") String username) {
        User user = searchService.getOneUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
