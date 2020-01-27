package edu.launchcode.asmrplaylist.controllers;

import com.google.api.services.youtube.model.Playlist;
import edu.launchcode.asmrplaylist.backend.client.YoutubeClient;
import edu.launchcode.asmrplaylist.backend.client.YoutubeVideoIDs;
import edu.launchcode.asmrplaylist.models.User;
import edu.launchcode.asmrplaylist.models.UserLogin;
import edu.launchcode.asmrplaylist.models.Video;
import edu.launchcode.asmrplaylist.repositories.UserDao;
import edu.launchcode.asmrplaylist.repositories.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;



@Controller
public class MyAsmrPlaylistController {

    //TODO need to work on getting rid of this variable and including it within each method.

    Long userId;

    // databases //

    @Autowired
    private UserDao userDao;

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private YoutubeClient youtubeClient;

    // user registration //

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String displaySignUpForm(Model model) {
        model.addAttribute("user", new User());
        return "signUpPage";
    }

    @RequestMapping(value = "registered", method = RequestMethod.POST)
    public String processSignUpForm(@NotNull Model model, @ModelAttribute @Valid User newUser, Errors errors,
                                    @RequestParam String name, @RequestParam String[] triggersList) {


        YoutubeVideoIDs youtubeVideoIDs = new YoutubeVideoIDs();
        List<Video> playlist = new ArrayList<>();
        String triggers = StringUtils.join(triggersList, " ");
        List<String> videoIds = youtubeVideoIDs.getVideoIDs(triggers);

        if (errors.hasErrors()) {
            return "signUpPage";
        }

        // TODO add code to delete null videoIds

        for (String videoId : videoIds) {
            Video video = new Video();
            video.setVideoId(videoId);
            playlist.add(video);
            videoDao.save(video);
            model.addAttribute("videoId", videoId);
        }

        newUser.setPlaylist(playlist);
        userDao.save(newUser);
        userId = newUser.getId();
        model.addAttribute("user", "Welcome, " + name);
        model.addAttribute("userId", userId); // see what happens when this is removed
        return "playPage";
    }

    // TODO: create an additional trigger form so I can hard code in the search term ASMR //

    // User LogIn //

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String displayLogInForm(Model model) {
        model.addAttribute("userLogin", new UserLogin());
        return "loginPage";
    }

    @RequestMapping(value = "loggedIn", method = RequestMethod.POST)
    public String processLogInForm(Model model, @ModelAttribute UserLogin newUserLogin, @RequestParam String userName,
                                   @RequestParam String password) {

        for (User user : userDao.findAll()) {
            if (user.getUserName().equals(userName) && user.getPassword().equals(password)) {

                List<Video> playlist = user.getPlaylist();
                userId = user.getId();

                for (Video video : playlist) {
                    model.addAttribute("videoId", video.getVideoId());
                    model.addAttribute("user", "Welcome, " + user.getName());
                    model.addAttribute("userId", user.getId());
                    System.out.println(userId);
                    return "playPage";
                }
            }
        }


        model.addAttribute("errorMessage", "Invalid Username or Password");
        return "loginPage";
    }

    // Homepage //

    @RequestMapping(value = "home")
    public String displayHomepage(Model model, @RequestParam Long userId) {

        Object user1 = userDao.findById(userId);
        System.out.println(user1);
        System.out.println("it worked!");

        for (User user : userDao.findAll()) {
            if (user.getId() == userId) {
                List<Video> playlist = user.getPlaylist();
                model.addAttribute("userId", userId);
                model.addAttribute("user", "Hi, " + user.getName());

                for (Video video : playlist) {
                    model.addAttribute("videoId", video.getVideoId());
                }
            }
        }

        return "playPage";
    }

//     view playlist //

    @RequestMapping(value = "playlist/{userId}")
    public String viewPlaylist(Model model, @PathVariable Long userId) {

        Object user1 = userDao.findById(userId);
        System.out.println(user1);

        ArrayList<String> playlist = new ArrayList<>();

        for (User user : userDao.findAll()) {

            if (user.getId() == userId) {
                System.out.println("sql found me");
                model.addAttribute("user", "Hi, " + user.getName());
                model.addAttribute("userId", userId);

                for (Video video : user.getPlaylist()) {
                    String videoId = video.getVideoId();
                    playlist.add(videoId);
                }
            }
        }
        model.addAttribute("playlist", playlist);
        return "playlistPage";
    }

    // create a query for videoId
    @RequestMapping(value = "playlist/{userId}/remove/{videoId}", method = RequestMethod.GET)
    public String displayRemoveVideoForm(Model model, @PathVariable Long userId, @PathVariable String videoId) {

        for (User user : userDao.findAll()) {
            if (user.getId() == userId) {

                for (Video video : user.getPlaylist()) {
                    if (video.getVideoId().equals(videoId)) {
                        videoDao.delete(video);
                    }
                }
            }
        }
        return "redirect:/playlist/" + userId;
    }

    //TODO: figure out how to add a video //
    // TODO: add functionality to create a new playlist //
    // TODO: refactor the template names
//
}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//