package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.core.service.UserService;

@Controller
public class UserController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public UserController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/users/{id}/comments")
    public String userComments(@PathVariable Long id, Model model) {
        model.addAttribute("comments", commentService.getCommentsByUser(id));
        model.addAttribute("username", userService.findById(id).getUsername());
        return "user-comments";
    }
}
