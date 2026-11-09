package kg.alatoo.tutorbooking.web;

import kg.alatoo.tutorbooking.dao.LessonDao;
import kg.alatoo.tutorbooking.dao.LessonFilter;
import kg.alatoo.tutorbooking.dao.StudentDao;
import kg.alatoo.tutorbooking.dao.SubjectDao;
import kg.alatoo.tutorbooking.dao.TutorDao;
import kg.alatoo.tutorbooking.model.Lesson;
import kg.alatoo.tutorbooking.model.LessonStatus;
import kg.alatoo.tutorbooking.service.LessonService;
import kg.alatoo.tutorbooking.service.TimeSlotTakenException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class LessonController {

    private final LessonService lessonService;
    private final LessonDao lessonDao;
    private final StudentDao studentDao;
    private final TutorDao tutorDao;
    private final SubjectDao subjectDao;

    public LessonController(LessonService lessonService, LessonDao lessonDao,
                            StudentDao studentDao, TutorDao tutorDao, SubjectDao subjectDao) {
        this.lessonService = lessonService;
        this.lessonDao = lessonDao;
        this.studentDao = studentDao;
        this.tutorDao = tutorDao;
        this.subjectDao = subjectDao;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/lessons";
    }

    @GetMapping("/lessons")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String tutorId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       Model model) {
        LessonFilter filter;
        try {
            filter = new LessonFilter(q, FormValues.parseOptionalInt(tutorId, "Репетитор"), parseStatus(status),
                    FormValues.parseOptionalDate(from, "С даты"), FormValues.parseOptionalDate(to, "По дату"));
            if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
                throw new IllegalArgumentException("Дата «с» позже даты «по»: поменяйте их местами");
            }
        } catch (IllegalArgumentException e) {
            // A broken filter must not show a misleading list: say what is wrong and show everything
            model.addAttribute("error", e.getMessage());
            filter = LessonFilter.none();
        }
        model.addAttribute("lessons", lessonDao.findAll(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("statuses", LessonStatus.values());
        model.addAttribute("summary", lessonDao.summary(LocalDate.now()));
        model.addAttribute("q", filter.studentName());
        model.addAttribute("students", studentDao.findAll());
        model.addAttribute("tutors", tutorDao.findAll());
        model.addAttribute("subjects", subjectDao.findAll());
        return "lessons";
    }

    private static LessonStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LessonStatus.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный статус: " + value);
        }
    }

    @GetMapping("/lessons/{id}/edit")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("lesson", lessonService.findBooked(id));
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/lessons";
        }
        model.addAttribute("students", studentDao.findAll());
        model.addAttribute("tutors", tutorDao.findAll());
        model.addAttribute("subjects", subjectDao.findAll());
        return "lesson-edit";
    }

    @PostMapping("/lessons/{id}/edit")
    public String edit(@PathVariable int id,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String tutorId,
                       @RequestParam(required = false) String subjectId,
                       @RequestParam(required = false) String startsAt,
                       @RequestParam(required = false) String location,
                       RedirectAttributes redirect) {
        try {
            Lesson lesson = lessonService.reschedule(id, type,
                    FormValues.parseInt(studentId, "Ученик"),
                    FormValues.parseInt(tutorId, "Репетитор"),
                    FormValues.parseInt(subjectId, "Предмет"),
                    FormValues.parseDateTime(startsAt, "Дата и время"),
                    location);
            redirect.addFlashAttribute("success",
                    "Занятие изменено: " + lesson.getStudent().getName() + ", " + lesson.getSubject()
                            + ", " + lesson.getPriceSom() + " сом");
            return "redirect:/lessons";
        } catch (IllegalArgumentException | TimeSlotTakenException e) {
            // Stay on the edit form, so the administrator can correct the time and try again
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/lessons/" + id + "/edit";
        }
    }

    @PostMapping("/lessons")
    public String create(@RequestParam(required = false) String type,
                         @RequestParam(required = false) String studentId,
                         @RequestParam(required = false) String tutorId,
                         @RequestParam(required = false) String subjectId,
                         @RequestParam(required = false) String startsAt,
                         @RequestParam(required = false) String location,
                         RedirectAttributes redirect) {
        try {
            Lesson lesson = lessonService.book(type,
                    FormValues.parseInt(studentId, "Ученик"),
                    FormValues.parseInt(tutorId, "Репетитор"),
                    FormValues.parseInt(subjectId, "Предмет"),
                    FormValues.parseDateTime(startsAt, "Дата и время"),
                    location);
            redirect.addFlashAttribute("success",
                    "Записано: " + lesson.getStudent().getName() + ", " + lesson.getSubject()
                            + ", " + lesson.getPriceSom() + " сом");
        } catch (IllegalArgumentException | TimeSlotTakenException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lessons";
    }

    @PostMapping("/lessons/{id}/done")
    public String markDone(@PathVariable int id, RedirectAttributes redirect) {
        try {
            lessonService.markDone(id);
            redirect.addFlashAttribute("success", "Занятие отмечено как проведённое");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lessons";
    }

    @PostMapping("/lessons/{id}/cancel")
    public String cancel(@PathVariable int id, RedirectAttributes redirect) {
        try {
            lessonService.cancel(id);
            redirect.addFlashAttribute("success", "Занятие отменено");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lessons";
    }
}
