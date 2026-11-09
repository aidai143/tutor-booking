package kg.alatoo.tutorbooking.web;

import kg.alatoo.tutorbooking.dao.StudentDao;
import kg.alatoo.tutorbooking.model.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentController {

    private final StudentDao studentDao;

    public StudentController(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    @GetMapping("/students")
    public String list(Model model) {
        model.addAttribute("students", studentDao.findAll());
        return "students";
    }

    @PostMapping("/students")
    public String create(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String grade,
                         RedirectAttributes redirect) {
        try {
            Student student = studentDao.save(new Student(name, phone, FormValues.parseInt(grade, "Класс")));
            redirect.addFlashAttribute("success", "Ученик добавлен: " + student);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // Redirect after POST, so refreshing the page does not submit the form twice
        return "redirect:/students";
    }

    @PostMapping("/students/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirect) {
        try {
            studentDao.deleteById(id);
            redirect.addFlashAttribute("success", "Ученик удалён");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/students";
    }
}
