package kg.alatoo.tutorbooking.service;

import kg.alatoo.tutorbooking.dao.LessonDao;
import kg.alatoo.tutorbooking.dao.StudentDao;
import kg.alatoo.tutorbooking.dao.SubjectDao;
import kg.alatoo.tutorbooking.dao.TutorDao;
import kg.alatoo.tutorbooking.model.Lesson;
import kg.alatoo.tutorbooking.model.LessonFactory;
import kg.alatoo.tutorbooking.model.LessonStatus;
import kg.alatoo.tutorbooking.model.Student;
import kg.alatoo.tutorbooking.model.Subject;
import kg.alatoo.tutorbooking.model.Tutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Booking rules live here, not in the DAO (which only talks SQL)
 * and not in the controller (which only handles the web page).
 */
@Service
public class LessonService {

    private final LessonDao lessonDao;
    private final StudentDao studentDao;
    private final TutorDao tutorDao;
    private final SubjectDao subjectDao;

    public LessonService(LessonDao lessonDao, StudentDao studentDao, TutorDao tutorDao, SubjectDao subjectDao) {
        this.lessonDao = lessonDao;
        this.studentDao = studentDao;
        this.tutorDao = tutorDao;
        this.subjectDao = subjectDao;
    }

    public Lesson book(String type, int studentId, int tutorId, int subjectId,
                       LocalDateTime startsAt, String location) {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));
        Tutor tutor = tutorDao.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));
        Subject subject = subjectDao.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));

        Lesson lesson = LessonFactory.create(type, student, tutor, subject, startsAt, location);

        if (startsAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя записать на прошедшее время");
        }
        if (lessonDao.tutorIsBusy(tutorId, startsAt)) {
            throw new TimeSlotTakenException("У репетитора " + tutor.getName() + " уже есть занятие в это время");
        }
        if (lessonDao.studentIsBusy(studentId, startsAt)) {
            throw new TimeSlotTakenException("У ученика " + student.getName() + " уже есть занятие в это время");
        }
        return lessonDao.save(lesson);
    }

    public void markDone(int lessonId) {
        changeStatus(lessonId, LessonStatus.DONE);
    }

    public void cancel(int lessonId) {
        changeStatus(lessonId, LessonStatus.CANCELLED);
    }

    private void changeStatus(int lessonId, LessonStatus newStatus) {
        if (!lessonDao.updateBookedStatus(lessonId, newStatus)) {
            throw new IllegalArgumentException("Занятие не найдено или уже проведено/отменено");
        }
    }
}
