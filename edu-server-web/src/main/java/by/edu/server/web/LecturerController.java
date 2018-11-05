package by.edu.server.web;

import by.edu.server.beans.courses.Course;
import by.edu.server.beans.courses.SubjectClass;
import by.edu.server.beans.enumerations.Role;
import by.edu.server.beans.people.User;
import by.edu.server.properties.resources.WebConstants;
import by.edu.server.services.api.CourseService;
import by.edu.server.services.api.LecturerService;
import by.edu.server.services.api.PersonService;
import by.edu.server.services.api.UserService;
import by.edu.server.utils.api.TokenManager;
import by.edu.server.web.dto.DtoShell;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/lecturer")
public class LecturerController {
    private static Logger logger = LogManager.getLogger(LecturerController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private PersonService personService;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private LecturerService lecturerService;
    @Autowired
    private CourseService courseService;
    @Value("${validation-fail}")
    private String validationFailed;

    private User defineUser(String token) {
        return tokenManager.get(token);
    }

    private Boolean checkRights(String token) {
        User user = defineUser(token);
        return user != null && user.getPerson().getRole().equals(Role.LECTURER);
    }

    @RequestMapping(value = "/schedule", method = RequestMethod.GET, produces = WebConstants.APPLICATION)
    public DtoShell<List<SubjectClass>> getSchedule(@RequestHeader(value = WebConstants.AUTHORIZATION) String token, @RequestBody DtoShell<List<Date>> request) {
        DtoShell<List<SubjectClass>> response = new DtoShell<>();
        if (!checkRights(token)) {
            response.setMessage(validationFailed);
            return response;
        }
        try {
            List<Date> dates = request.getData();
            Date from;
            Date to;
            if (dates.size() < 1) {
                return response;
            } else {
                from = dates.get(0);
                if (dates.size() > 1) {
                    to = dates.get(1);
                } else {
                    to = new Date(System.currentTimeMillis());
                }
            }
            response.setData(lecturerService.getSchedule(defineUser(token).getId(), from, to));
            response.setSuccessful(true);
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.getMessage());
        }
        return response;
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET, produces = WebConstants.APPLICATION)
    public DtoShell<Course> getCourse(@RequestHeader(value = WebConstants.AUTHORIZATION) String token, @RequestBody DtoShell<Integer> courseId) {
        DtoShell<Course> response = new DtoShell<>();
        if (!checkRights(token)) {
            response.setMessage(validationFailed);
            return response;
        }
        try {
            response.setData(courseService.getCourse(courseId.getData(), true));
            response.setSuccessful(true);
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.getMessage());
        }
        return response;
    }

    @RequestMapping(value = "/course", method = RequestMethod.POST, produces = WebConstants.APPLICATION)
    public DtoShell<String> editCourse(@RequestHeader(value = WebConstants.AUTHORIZATION) String token, @RequestBody DtoShell<Course> courseDto) {
        DtoShell<String> response = new DtoShell<>();
        if (!checkRights(token)) {
            response.setMessage(validationFailed);
            return response;
        }
        try {
            courseService.updateCourse(courseDto.getData());
            response.setData("");
            response.setSuccessful(true);
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.getMessage());
        }
        return response;
    }

}
