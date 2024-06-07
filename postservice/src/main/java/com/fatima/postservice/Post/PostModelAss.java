package com.fatima.postservice.Post;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fatima.postservice.Controllers.PostController;
import com.fatima.postservice.Comment.Comment;
import com.fatima.postservice.Like.Like;
import com.fatima.postservice.Share.Share;
import com.fatima.postservice.User;
import com.fatima.postservice.UserServiceFeignClient;
import com.fatima.postservice.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
@Component
public class PostModelAss implements RepresentationModelAssembler<Post, EntityModel<Post>> {
  @Autowired
     HttpServletRequest request;
    @Autowired
    PostRepo postRepo;
  
    @Autowired
    JwtUtils jwtUtils;


    @Autowired
    private UserServiceFeignClient UserServiceFeignClient;

    @Override
    public EntityModel<Post> toModel(Post post) {
       
       return EntityModel.of(post,
                linkTo(methodOn(PostController.class).sharePost(post.getPostId(),null)).withRel("createShare"),
                linkTo(methodOn(PostController.class).createComment(null ,post.getPostId())).withRel("createComment"),
              linkTo(methodOn(PostController.class).createLikePost(null ,post.getPostId())).withRel("create like"),
              linkTo(methodOn(PostController.class).findByLikesContainsUser()).withRel("is liked"),
              linkTo(methodOn(PostController.class).numberComment(post.getPostId())).withRel("number of comment"),
              linkTo(methodOn(PostController.class).numberLike(post.getPostId())).withRel("number of like"),
              linkTo(methodOn(PostController.class).checkIfUserLikedPost(post.getPostId())).withRel("If User Liked Post"),
              linkTo(methodOn(PostController.class).getRandomLikes(post.getPostId())).withRel("the post's likes"),

               linkTo(methodOn(PostController.class).getRandomComments(post.getPostId())).withRel("the post's comment"));


    }

   
        public EntityModel<Post> toModelpostId(Post post)  {
             User user=null;
          String jwt = parseJwt(request);
       
           if (userHasPermissionToDeletePost(post.getPostId(),user.getId())){
           return EntityModel.of(post,
          linkTo(methodOn(PostController.class).getAllPostLikes(post.getPostId())).withRel("the post's like"),
          linkTo(methodOn(PostController.class).getAllPostComments(post.getPostId())).withRel("the post's comment"),
          linkTo(methodOn(PostController.class).deleteById(post.getPostId())).withRel("delete your post"));
        }else{    return EntityModel.of(post,
                   linkTo(methodOn(PostController.class).getAllPostLikes(post.getPostId())).withRel("the post's like"),
                   linkTo(methodOn(PostController.class).getAllPostComments(post.getPostId())).withRel("the post's comment"));
           }
    
          

        } 
         

        public EntityModel<Like> toModelPostLike(Like like)  {
         // User user=null;
            String jwt = parseJwt(request);
             
                  String username = jwtUtils.getUserNameFromJwtToken(jwt);
             
             Post post = like.post;
     
             if ( UserServiceFeignClient.getUserByUserId(like.getUserId()) ==UserServiceFeignClient.getUsersByUsername(username)){
             return EntityModel.of(like,
             linkTo(methodOn(PostController.class).findById(post.getPostId())).withRel("the post "),
           // linkTo(methodOn(PostController.class).getAllPostLikes(like.getPostId())).withRel("the post's like"),
           linkTo(methodOn(PostController.class).getRandomLikes(post.getPostId())).withRel("read more"),
            linkTo(methodOn(PostController.class).getAllPostComments(post.getPostId())).withRel("the post's comment"),
            linkTo(methodOn(PostController.class).UnCreatelikePost(like.getLikeId())).withRel("delete your like"));
          }else{    return EntityModel.of(like,
            linkTo(methodOn(PostController.class).findById(post.getPostId())).withRel("the post "),
            linkTo(methodOn(PostController.class).getRandomLikes(post.getPostId())).withRel("read more"),
            linkTo(methodOn(PostController.class).getAllPostComments(post.getPostId())).withRel("the post's comment"));   
         }
      
            
          }

    

private boolean userHasPermissionToDeletePost(Long postId, Long userId) {
     
        Post post = postRepo.findById(postId).orElse(null);

        return post != null && post.getUserId().equals(userId);
    }



 private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;


  }

  

        public EntityModel<Share> toModelsharepostId(Share share)  {
             User user = userFromToken(request);
    if (user!=null){   
        if (UserServiceFeignClient.getUserByUserId(share.getUserId()).equals(user.getId())){
           return EntityModel.of(share,
           linkTo(methodOn(PostController.class).findById(share.getPost().getPostId())).withRel("the post you shared"),
          // linkTo(methodOn(PostController.class).getAllPostLikes(share.getPostId())).withRel("the post's like"),
          // linkTo(methodOn(PostController.class).getAllPostComments(share.getPostId(),request)).withRel("the post's comment"),
          linkTo(methodOn(PostController.class).deleteShearById(share.getShareId())).withRel("delete your share post"));
        }else{    return EntityModel.of(share,
                    linkTo(methodOn(PostController.class).findById(share.getPost().getPostId())).withRel("the post you shared"));
                  //  linkTo(methodOn(PostController.class).getAllPostLikes(share.getPostId())).withRel("the post's like"),
                  //  linkTo(methodOn(PostController.class).getAllPostComments(share.getPostId(),request)).withRel("the post's comment"));
    
       } } return EntityModel.of(share,
       linkTo(methodOn(PostController.class).findAllPost()).withRel("Go to the all post"));}


public User userFromToken(HttpServletRequest request){
  String jwt = parseJwt(request);
  if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
      String username = jwtUtils.getUserNameFromJwtToken(jwt);
      User user = UserServiceFeignClient.getUsersByUsername(username);
            return user;}
  return null;
}





}

