package kg.alatoo.tutorbooking.web;

import kg.alatoo.tutorbooking.dao.DatabaseException;
import kg.alatoo.tutorbooking.dao.LessonDao;
import kg.alatoo.tutorbooking.dao.StudentDao;
import kg.alatoo.tutorbooking.dao.SubjectDao;
import kg.alatoo.tutorbooking.dao.TutorDao;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/**
 * Adds the counters for the left menu ("Ученики 12") to every page,
 * so each controller does not have to do it itself.
 */
@ControllerAdvice
public class NavCountsAdvice {

    private final LessonDao lessonDao;
    private final StudentDao studentDao;
    private final TutorDao tutorDao;
    private final SubjectDao subjectDao;

    public NavCountsAdvice(LessonDao lessonDao, StudentDao studentDao, TutorDao tutorDao, SubjectDao subjectDao) {
        this.lessonDao = lessonDao;
        this.studentDao = studentDao;
        this.tutorDao = tutorDao;
        this.subjectDao = subjectDao;
    }

    @ModelAttribute("navCounts")
    public Map<String, Integer> navCounts() {
        try {
            return Map.of(
                    "lessons", lessonDao.findAll(null).size(),
                    "students", studentDao.findAll().size(),
                    "tutors", tutorDao.findAll().size(),
                    "subjects", subjectDao.findAll().size());
        } catch (DatabaseException e) {
            // The menu must still render (e.g. on the error page) when the database is down
            return Map.of();
        }
    }
}
