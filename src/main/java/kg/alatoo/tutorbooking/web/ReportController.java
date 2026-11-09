package kg.alatoo.tutorbooking.web;

import kg.alatoo.tutorbooking.dao.LessonDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

    private final LessonDao lessonDao;

    public ReportController(LessonDao lessonDao) {
        this.lessonDao = lessonDao;
    }

    @GetMapping("/report")
    public String report(Model model) {
        model.addAttribute("rows", lessonDao.tutorReport());
        return "report";
    }
}
