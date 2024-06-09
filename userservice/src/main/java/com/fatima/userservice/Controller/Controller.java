package com.fatima.userservice.Controller;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.fatima.userservice.NFException;
// package com.al7komaaa.userservice.Entity.Comment.Comment;
import com.fatima.userservice.Controller.ImageUploadController;

// package com.al7komaaa.userservice.Entity.Comment.CommentRepo;
// package com.al7komaaa.userservice.Entity.Like.Like;
// package com.al7komaaa.userservice.Entity.Like.LikeRepo;
// package com.al7komaaa.userservice.Entity.Post.Post;
// package com.al7komaaa.userservice.Entity.Post.PostRepo;
// package com.al7komaaa.userservice.Entity.Share.Share;
// package com.al7komaaa.userservice.Entity.Share.ShareRepo;
import com.fatima.userservice.User.Gender;
import com.fatima.userservice.User.User;
import com.fatima.userservice.User.UserModelAss;
import com.fatima.userservice.User.UserRepo;
import com.fatima.userservice.User.Friend.FriendRequest;
import com.fatima.userservice.User.Friend.FriendRequestRepo;
import com.fatima.userservice.Payload.Response.MessageResponse;
import com.fatima.userservice.Security.Jwt.JwtUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.UUID;
@RequestMapping("/users")

@RestController
public class Controller {
    // private final CommentRepo commentRepo;
    // private final LikeRepo likeRepo;
    // private final PostRepo postRepo;
    // private final ShareRepo shareRepo;
    @Autowired
    private  UserRepo userRepo;

@Autowired
private FriendRequestRepo friendRequestRepo;
      @Autowired
  private JwtUtils jwtUtils;
    @Autowired
private UserModelAss userModelAss;
@Autowired
HttpServletRequest request;

@Autowired
ImageUploadController imageUploadController;

// @Autowired
// private NotificationController notificationController;

// ImageUploadController imageUploadController=new ImageUploadController();
//     public Controller(CommentRepo commentRepo, LikeRepo likeRepo, PostRepo postRepo, ShareRepo shareRepo,
//             UserRepo userRepo) {
//         this.commentRepo = commentRepo;
//         this.likeRepo = likeRepo;
//         this.postRepo = postRepo;
//         this.shareRepo = shareRepo;
//         this.userRepo = userRepo;
       
//     }






 // إرسال طلب صداقة
 @PostMapping("/sendFriendRequest/{receiverId}")
 public ResponseEntity<String> sendFriendRequest(HttpServletRequest request, @PathVariable Long receiverId) {
     User sender = userFromToken(request);
     User receiver = userRepo.findById(receiverId)
             .orElseThrow(() -> new NFException("User not found."));
 
     // تحقق من وجود طلب صداقة معلق من المرسل إلى المستقبل
     if (friendRequestRepo.findBySenderIdAndReceiverId(sender.getId(), receiverId).isPresent()) {
         return ResponseEntity.ok("Friend request already sent.");
     }
 
     // تحقق من وجود طلب صداقة معلق من المستقبل إلى المرسل
     Optional<FriendRequest> friendRequestFromReceiver = friendRequestRepo.findBySenderIdAndReceiverId(receiverId, sender.getId());
     if (friendRequestFromReceiver.isPresent()) {
         return ResponseEntity.ok("This friend has already sent you a friend request.");
     }
 
     FriendRequest friendRequest = new FriendRequest();
     friendRequest.setSender(sender);
     friendRequest.setReceiver(receiver);
 
     friendRequestRepo.save(friendRequest);
 
     return ResponseEntity.ok("Friend request sent successfully.");
 }
 


    // عرض طلبات الصداقة المعلقة
    @GetMapping("/friendRequests")
    public ResponseEntity<List<FriendRequest>> getFriendRequests(HttpServletRequest request) {
        User receiver = userFromToken(request);
        List<FriendRequest> friendRequests = friendRequestRepo.findByReceiverId(receiver.getId());
        return ResponseEntity.ok(friendRequests);
    }

    // قبول طلب صداقة
    @PostMapping("/acceptFriendRequest/{senderId}")
    public ResponseEntity<String> acceptFriendRequest(HttpServletRequest request, @PathVariable Long senderId) {
        User receiver = userFromToken(request);
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new NFException("User not found."));

        FriendRequest friendRequest = friendRequestRepo.findBySenderIdAndReceiverId(senderId, receiver.getId())
                .orElseThrow(() -> new NFException("Friend request not found."));

        // أضف الصداقة في كلا الحسابين
        receiver.friends.add(sender);
        sender.friends.add(receiver);

        userRepo.save(receiver);
        userRepo.save(sender);

        // احذف طلب الصداقة بعد قبوله
        friendRequestRepo.delete(friendRequest);

        return ResponseEntity.ok("Friend request accepted.");
    }



    // @DeleteMapping("/cancelFriendRequest/{userId}")
    // public ResponseEntity<String> cancelFriendRequest( @PathVariable Long userId) {
    //     User currentUser = userFromToken(request);
    //     User otherUser = userRepo.findById(userId)
    //             .orElseThrow(() -> new NFException("User not found."));

    //     Optional<FriendRequest> friendRequestOpt = friendRequestRepo.findBySenderAndReceiver(otherUser, currentUser);

    //     if (friendRequestOpt.isPresent()) {
    //         FriendRequest friendRequest = friendRequestOpt.get();
    //         friendRequestRepo.deleteById(friendRequest.getId());
    //         return ResponseEntity.ok("Friend request cancelled successfully.");
    //     } else {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend request not found.");
    //     }
    // }

  

    // @DeleteMapping("/cancelFriendRequest2/{userId}")
    // public ResponseEntity<String> cancelFriendRequest2(HttpServletRequest request, @PathVariable Long userId) {
    //     User currentUser = userFromToken(request);
    //     User otherUser = userRepo.findById(userId)
    //             .orElseThrow(() -> new NFException("User not found."));
    //             FriendRequest friendRequest = friendRequestRepo.findBySenderIdAndReceiverId(otherUser.getId(), currentUser.getId())
    //             .orElseThrow(() -> new NFException("Friend request not found."));
    //             FriendRequest friendRequest2 = friendRequestRepo.findBySenderIdAndReceiverId( currentUser.getId(),otherUser.getId())
    //             .orElseThrow(() -> new NFException("Friend request not found."));

    //             friendRequestRepo.delete(friendRequest);
    //             friendRequestRepo.delete(friendRequest2);

    //     return ResponseEntity.ok("All friend requests between the users have been cancelled successfully.");
    // }








    @GetMapping("/users")
    public ResponseEntity<CollectionModel<EntityModel<User>>> getAllUsers(HttpServletRequest request) {
        List<EntityModel<User>> users = userRepo.findAll().stream()
                .map(user -> userModelAss.toModelfriendself(user))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(users, linkTo(methodOn(Controller.class).getAllUsers(request)).withSelfRel()));
    }
 
    

    @GetMapping("/user/{id}")
public ResponseEntity<?> getUserById(@PathVariable Long id) {

    User user = userRepo.findById(id)
    .orElseThrow(() -> new NFException("user with ID " + id + " not found."));
    EntityModel<User> entityModel = userModelAss.toModeluserprofile(user);
    // if (user.getAccountIsPrivate()||user!=me||!me.friends.contains(user))
    // return ResponseEntity.ok("this account is private to see user post addFriend");
    return ResponseEntity.ok(entityModel);
}

// اغير الريسبونس انتتي

  
    @GetMapping("/userByFirstName/{name}")
    public ResponseEntity<?> getUserByFirstName(@PathVariable String name) {
        List<User> userList = userRepo.findByFirstname(name);
      //  EntityModel<User> entityModel = userModelAss.toModeluserprofile(name, request);
      List<EntityModel<User>> users = userList.stream()
      .map(user -> userModelAss.toModelfriendself(user))
      .collect(Collectors.toList());
        if (users.isEmpty()) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("there are no user with firstname "+ name));
        }
        
        return ResponseEntity.ok(users);
      
    }

    @GetMapping("/userByLastName/{name}")// netzakar eno ne3malha non-CaseSensitive
    public ResponseEntity<?> getUserByLastName(@PathVariable String name) {
        List<User> userList = userRepo.findByLastname(name);
        List<EntityModel<User>> users = userList.stream()
      .map(user -> userModelAss.toModelfriendself(user))
      .collect(Collectors.toList());
        if (users.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("there are no user with Lastname "+ name));
        }
        return ResponseEntity.ok(users);
      
    }

  @GetMapping("/fullName/{name}")
public ResponseEntity<?>getFullName(@PathVariable String name) {
    List<User> userList = new ArrayList<>();
    userList.addAll(userRepo.findByFirstname(name));
    userList.addAll(userRepo.findByLastname(name));
    userList.addAll(userRepo.findByFullname(name));
    List<EntityModel<User>> users = userList.stream()
    .map(user -> userModelAss.toModelfriendself(user))
    .collect(Collectors.toList());
      if (users.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("there are no user with fullname "+ name));
      }
      return ResponseEntity.ok(users);
    
}
       
@GetMapping("/UserName/{name}")
public ResponseEntity<?> getUserName(@PathVariable String name ) {
    User user = userRepo.findByUsername(name) .orElseThrow(() -> new NFException("user not found."));;
   
        EntityModel<User> entityModel = userModelAss.toModeluserprofile(user);
        // if (user.get().getAccountIsPrivate())
        // return ResponseEntity.ok("this account is private to see user post addFriend");
       return ResponseEntity.ok(entityModel);
    
    
}



@GetMapping("/count/userFriend/{userid}")
public ResponseEntity<Long> getUserFriendcount(@PathVariable Long userid ){

   
return ResponseEntity.ok(userRepo.countFriends(userid));

}
@GetMapping("/userFriend/{userid}")
public ResponseEntity<CollectionModel<EntityModel<User>>> getUserFriend(@PathVariable Long userid ){

    User user = userRepo.findById(userid) .orElseThrow(() -> new NFException("user not found."));
    List<EntityModel<User>> users =userRepo.getFriends(userid).stream()
    .map(us -> userModelAss.toModelfriendself(us))
    .collect(Collectors.toList());
// return userRepo.getFriends(userid);
return ResponseEntity.ok(CollectionModel.of(users, linkTo(methodOn(Controller.class).getUserById(userid)).withRel("Go to all Posts")));

}
@GetMapping("/friendSuggestion")
public List<EntityModel<User>> getfriendSuggestion(){

    User user = userFromToken(request);
    List<EntityModel<User>> users =userRepo.findSuggestedFriends(user.getId()).stream()
    .map(us -> userModelAss.toModelfriendself(us))
    .collect(Collectors.toList());
// return userRepo.getFriends(userid);
return users;

}


@GetMapping("/myUserName")
public User getmyUserName( ){

    User user = userFromToken(request);
return user;
//  ResponseEntity.ok("Fatma");

}

@GetMapping("/hasSentFriendRequest/{userId}")
    public ResponseEntity<Boolean> hasSentFriendRequest( @PathVariable Long userId) {
        User currentUser = userFromToken(request);
        User otherUser = userRepo.findById(userId)
                .orElseThrow(() -> new NFException("User not found."));

        boolean hasSentRequest = friendRequestRepo.findBySenderAndReceiver(currentUser, otherUser).isPresent();

        return ResponseEntity.ok(hasSentRequest);
        
}

@GetMapping("/isFriend/{userId}")
    public ResponseEntity<Boolean> isFriend(@PathVariable Long userId) {
        User currentUser = userFromToken(request);
        User otherUser = userRepo.findById(userId)
                .orElseThrow(() -> new NFException("User not found."));

        boolean isFriend = currentUser.friends.contains(otherUser);

        return ResponseEntity.ok(isFriend);
      
}
@DeleteMapping("/cancelFriendRequest/{userId}")
public ResponseEntity<String> cancelFriendRequest( @PathVariable Long userId) {
  User currentUser = userFromToken(request);
  User otherUser = userRepo.findById(userId)
          .orElseThrow(() -> new NFException("User not found."));

  Optional<FriendRequest> friendRequestOpt = friendRequestRepo.findBySenderAndReceiver(currentUser, otherUser);

  if (friendRequestOpt.isPresent()) {
      FriendRequest friendRequest = friendRequestOpt.get();
      friendRequestRepo.deleteById(friendRequest.getId());
      return ResponseEntity.ok("Friend request cancelled successfully.");
  } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend request not found.");
}
}

@DeleteMapping("/cancelFriendRequest/res/{userId}")
public ResponseEntity<String> cancelFriendRequestres( @PathVariable Long userId) {
  User currentUser = userFromToken(request);
  User otherUser = userRepo.findById(userId)
          .orElseThrow(() -> new NFException("User not found."));

  Optional<FriendRequest> friendRequestOpt = friendRequestRepo.findBySenderAndReceiver( otherUser,currentUser);

  if (friendRequestOpt.isPresent()) {
      FriendRequest friendRequest = friendRequestOpt.get();
      friendRequestRepo.deleteById(friendRequest.getId());
      return ResponseEntity.ok("Friend request cancelled successfully.");
  } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend request not found.");
}
}
@PostMapping("/addUserFriend/{userfriendid}")
public ResponseEntity<String> addFriend(HttpServletRequest request, @PathVariable Long userfriendid) {

    String jwt = parseJwt(request);
    if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepo.findByUsername(username)
        .orElseThrow(() -> new NFException("user not found."));
        User friend = userRepo.findById(userfriendid)
        .orElseThrow(() -> new NFException("Friend not found."));

        // Check if the friend already exists in the user's friends list
        boolean alreadyExists = user.friends.contains(friend);
        boolean alreadyExistss = friend.friends.contains(user);
        // If the friend doesn't already exist, add them to the user's friends list
        if (!alreadyExists && !alreadyExistss ) {
            // friend.setFriend(true);
            // user.setFriend(true);
            user.friends.add(friend);
            friend.friends.add(user);
            userRepo.save(friend);
            userRepo.save(user);
            return ResponseEntity.ok("Friend added successfully");
        } else {
            return ResponseEntity.badRequest().body("Friend already exists");
        }
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("failed process");
}



@DeleteMapping("/deleteUserFriend/{userid}")
public ResponseEntity<String> deleteUserFriend(@PathVariable Long userid, HttpServletRequest request) {

    String jwt = parseJwt(request);
    if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepo.findByUsername(username)
        .orElseThrow(() -> new NFException("user not found."));
        User friend = userRepo.findById(userid)
        .orElseThrow(() -> new NFException("Friend not found."));

        boolean alreadyExists = user.friends.contains(friend);
        boolean alreadyExistss = friend.friends.contains(user);

        if (alreadyExists && alreadyExistss) {
            // friend.setFriend(false);
            // user.setFriend(false);
            user.friends.remove(friend);
            friend.friends.remove(user);
            userRepo.save(friend);
            userRepo.save(user);
            return ResponseEntity.ok("Friend removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Friend not found in user's friend list");
        }
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
}


@PutMapping("/privacy")
public ResponseEntity<?> setPrivacy(@RequestBody boolean isprivate) {
    User user = userFromToken(request);
        if (user==null)
return ResponseEntity.ok("make sure you signed up");
user.setAccountIsPrivate(isprivate);
userRepo.save(user);
  return ResponseEntity.ok("the privacy of account is "+ isprivate);
}





 private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }

  public User userFromToken(HttpServletRequest request) {
    String jwt = parseJwt(request);
    if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        return userRepo.findByUsername(username)
        .orElseThrow(() -> new NFException("user not found."));
    }
    return null;
}

@GetMapping("/userFromToken")
public User userFromTokenpost(HttpServletRequest request) {
    String jwt = parseJwt(request);
    if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        return userRepo.findByUsername(username)
        .orElseThrow(() -> new NFException("user not found."));
    }
    return null;

}

// @GetMapping("/search/{username}")
// public ResponseEntity<List<User>> getUsersByUsername(@PathVariable String username) {
//     List<User> users = userRepo.searchUsers(username);
//     return ResponseEntity.ok(users);
// }

@GetMapping("/search/{username}")
public ResponseEntity<?> searchUsersByUsername(@PathVariable String username) {
    List<User> users = userRepo.searchUsers(username);
    if (users.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("No users found with username " + username));
    }
    return ResponseEntity.ok(users);
}

@PostMapping("/saveUser")
public void saveUser( User user ) {
userRepo.save(user)   ; 
}

@GetMapping("/user")
public User user(Long id) {
  return userRepo.findById(id).get();
   
}

@GetMapping("/user")
    public ResponseEntity<?> getOneUserByUsername(@RequestParam String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NFException("User with username " + username + " not found."));
        return ResponseEntity.ok(user);
    }




@GetMapping("/accountIsPrivate/{userId}")
public boolean getaccountIsPrivate(@PathVariable Long userId) {
User user=userRepo.findById(userId).get();
  User me=userFromToken(request);
  if (me.friends.contains(user)||!user.getAccountIsPrivate()||me==user)
return false;
else
   return true;
}  @PutMapping("/editProfile")
public ResponseEntity<String> editProfile(@RequestBody User user) {
    User myuser = userFromToken(request);
    if (myuser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    myuser.setEmail(user.getEmail());
    myuser.setBio(user.getBio());
    myuser.setDateofbirth(user.getDateofbirth());
    myuser.setGender(user.getGender());
    myuser.setMobile(user.getMobile());
    myuser.setLocation(user.getLocation());
    myuser.setFirstname(user.getFirstname());
    myuser.setLastname(user.getLastname());
    
    userRepo.save(myuser);

    return ResponseEntity.ok("User Details have been changed!!");
}





  
@PutMapping("/editFirstName")
public ResponseEntity<?> editFirstName(@RequestBody String newfirstName) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setFirstname(newfirstName);
userRepo.save(user);
  return ResponseEntity.ok("firstName changed to" +user.getFirstname());
}
@PutMapping("/editLastName")
public ResponseEntity<?> editLastName(@RequestBody String newlastName) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setLastname(newlastName);
userRepo.save(user);
  return ResponseEntity.ok("lastName changed to" +user.getLastname());
}

@PutMapping("/editMobile")
public ResponseEntity<?> editMobile(@RequestBody String mobile) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setMobile(mobile);
userRepo.save(user);
  return ResponseEntity.ok("Mobile changed to" +user.getMobile());
}

@PutMapping("/editEmail")
public ResponseEntity<?> editEmail(@RequestBody String email) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setEmail(email);
userRepo.save(user);
  return ResponseEntity.ok("Email changed to" +user.getEmail());
}


// @PutMapping("/editUsername")
// public ResponseEntity<?> editUsername(@RequestBody String username) {
//   User user = userFromToken(request);
//   if (user==null)
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
// user.setUsername(username);
// userRepo.save(user);
//   return ResponseEntity.ok("Username changed to" +user.getUsername());
// }


@PutMapping("/editGender")
public ResponseEntity<?> editGender(@RequestBody Gender gender) { //
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setGender(gender);
userRepo.save(user);
  return ResponseEntity.ok("Gender changed to" +user.getGender());
}


@PutMapping("/editBio")
public ResponseEntity<?> editBio(@RequestBody String bio) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setBio(bio);
userRepo.save(user);
  return ResponseEntity.ok("Bio changed to" +user.getBio());
}



@PutMapping("/editLocation")
public ResponseEntity<?> editLocation(@RequestBody String location) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setLocation(location);
userRepo.save(user);
  return ResponseEntity.ok("Location changed to" +user.getLocation());
}


@PutMapping("/editImage")
public ResponseEntity<?> editImage(@RequestParam("file")MultipartFile file) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
String res=imageUploadController.uploadImage(file);
user.setImage(res);
userRepo.save(user);
  return ResponseEntity.ok(user.getImage());
}
@GetMapping("/getImage")
public String getUserImage() {
  User user = userFromToken(request);
  return user.getImage();

}
@GetMapping("/getImage/{userId}")
public ResponseEntity<byte[]> getImage(@PathVariable Long userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user == null || user.getImage() == null || user.getImage().isEmpty()) {
      Path imagePath = Paths.get( "49e40f05-46ad-42b6-a2f3-6270d67cb6df_download.jpeg");
      byte[] imageBytes;
      try {
        imageBytes = Files.readAllBytes(imagePath); 
         return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    
    }
    try {
        Path imagePath = Paths.get( user.getImage());
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

@PutMapping("/editDof")
public ResponseEntity<?> editDof(@RequestBody LocalDate dateofbirth) {//
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
user.setDateofbirth(dateofbirth);
userRepo.save(user);
  return ResponseEntity.ok("Date of birth changed to" +user.getDateofbirth());
}


@PutMapping("/editBackgroundImage")
public ResponseEntity<?> editBackgroundImage(@RequestParam("file")MultipartFile file) {
  User user = userFromToken(request);
  if (user==null)
return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
String res=imageUploadController.uploadImage(file);
user.setBackgroudimage(res);
userRepo.save(user);
  return ResponseEntity.ok(user.getBackgroudimage());
}

@GetMapping("/backgroundImage/{userId}")
public ResponseEntity<?> getBackgroundImage(@PathVariable Long userId) {
  User user = userRepo.findById(userId) .orElseThrow(() -> new NFException("user with ID " + userId + " not found."));

  if (user == null || user.getBackgroudimage() == null || user.getBackgroudimage().isEmpty()) {
    Path imagePath = Paths.get( "7ba92ff8-08bf-4836-ad5c-074835f8288f_download.jpg");
    
    try {
     byte[]  imageBytes = Files.readAllBytes(imagePath); 
       return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  
  }
  try {
      Path imagePath = Paths.get( user.getBackgroudimage());
      byte[] imageBytes = Files.readAllBytes(imagePath);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
  } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}


 @PutMapping("/editMode")
    public ResponseEntity<Boolean> editMode() {
        User user = userFromToken(request);
        if (user == null) {
            return ResponseEntity.ok(false);
        }
        user.setDark(!user.isDark());
        userRepo.save(user);
        return ResponseEntity.ok(user.isDark());
    }








}







