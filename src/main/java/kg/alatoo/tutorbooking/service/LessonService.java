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
        Lesson lesson = buildChecked(type, studentId, tutorId, subjectId, startsAt, location, LessonDao.NO_LESSON);
        return lessonDao.save(lesson);
    }

    /**
     * Moves a planned lesson: new time, people or format. The same rules as for booking apply,
     * the price is calculated again (the tutor or the format may have changed).
     */
    public Lesson reschedule(int lessonId, String type, int studentId, int tutorId, int subjectId,
                             LocalDateTime startsAt, String location) {
        Lesson current = findBooked(lessonId);
        Lesson lesson = buildChecked(type, studentId, tutorId, subjectId, startsAt, location, current.getId());
        lesson.setId(current.getId());
        if (!lessonDao.update(lesson)) {
            throw new IllegalArgumentException("Занятие не найдено или уже проведено/отменено");
        }
        return lesson;
    }

    /** Only planned lessons can be changed: done and cancelled ones are history. */
    public Lesson findBooked(int lessonId) {
        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));
        if (lesson.getStatus() != LessonStatus.BOOKED) {
            throw new IllegalArgumentException("Изменить можно только запланированное занятие");
        }
        return lesson;
    }

    // Shared by booking and rescheduling, so both follow exactly the same rules
    private Lesson buildChecked(String type, int studentId, int tutorId, int subjectId,
                                LocalDateTime startsAt, String location, int excludeLessonId) {
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
        if (lessonDao.tutorIsBusy(tutorId, startsAt, excludeLessonId)) {
            throw new TimeSlotTakenException("У репетитора " + tutor.getName() + " уже есть занятие в это время");
        }
        if (lessonDao.studentIsBusy(studentId, startsAt, excludeLessonId)) {
            throw new TimeSlotTakenException("У ученика " + student.getName() + " уже есть занятие в это время");
        }
        return lesson;
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
