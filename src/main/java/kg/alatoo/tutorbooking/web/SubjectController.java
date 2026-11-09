package kg.alatoo.tutorbooking.web;

import kg.alatoo.tutorbooking.dao.SubjectDao;
import kg.alatoo.tutorbooking.model.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SubjectController {

    private final SubjectDao subjectDao;

    public SubjectController(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @GetMapping("/subjects")
    public String list(Model model) {
        model.addAttribute("subjects", subjectDao.findAll());
        return "subjects";
    }

    @PostMapping("/subjects")
    public String create(@RequestParam(required = false) String name, RedirectAttributes redirect) {
        try {
            Subject subject = subjectDao.save(new Subject(name));
            redirect.addFlashAttribute("success", "Предмет добавлен: " + subject);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/subjects";
    }

    @PostMapping("/subjects/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirect) {
        try {
            subjectDao.deleteById(id);
            redirect.addFlashAttribute("success", "Предмет удалён");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/subjects";
    }
}
