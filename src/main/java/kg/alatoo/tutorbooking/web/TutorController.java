package kg.alatoo.tutorbooking.web;

import kg.alatoo.tutorbooking.dao.TutorDao;
import kg.alatoo.tutorbooking.model.Tutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TutorController {

    private final TutorDao tutorDao;

    public TutorController(TutorDao tutorDao) {
        this.tutorDao = tutorDao;
    }

    @GetMapping("/tutors")
    public String list(Model model) {
        model.addAttribute("tutors", tutorDao.findAll());
        return "tutors";
    }

    @PostMapping("/tutors")
    public String create(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String hourlyRateSom,
                         RedirectAttributes redirect) {
        try {
            Tutor tutor = tutorDao.save(new Tutor(name, phone, FormValues.parseInt(hourlyRateSom, "Ставка")));
            redirect.addFlashAttribute("success", "Репетитор добавлен: " + tutor);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tutors";
    }

    @PostMapping("/tutors/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirect) {
        try {
            tutorDao.deleteById(id);
            redirect.addFlashAttribute("success", "Репетитор удалён");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tutors";
    }
}
