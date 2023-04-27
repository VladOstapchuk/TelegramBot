package academy.prog.javaprobot.Controllers;

import academy.prog.javaprobot.User;
import academy.prog.javaprobot.UserService;
import academy.prog.javaprobot.webapp.dto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static academy.prog.javaprobot.Utils.isValidUkrainianPhoneNumber;

@RestController
public class webcontroller {
    private final UserService userService;
    private static final int PAGE_SIZE = 5;

    public webcontroller(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("users")
    public dto count() {
        return dto.of(userService.getUsersCount(), PAGE_SIZE);
    }

    @GetMapping("allusers")
    public List<User> locations(@RequestParam(required = false, defaultValue = "0") int page) {
        return userService.findAllUsers(Pageable.ofSize(PAGE_SIZE));
    }

    @GetMapping("/delete")
    public void locations(@RequestParam(required = false, defaultValue = "") String phone) {
        if (isValidUkrainianPhoneNumber(phone)) {
            userService.deleteUser(userService.findUserByPhone(phone).get());
        }

    }
}
