package feature.test;

import com.codeborne.selenide.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import ru.yandex.qatools.allure.annotations.Step;


import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.empty;
import static com.codeborne.selenide.CollectionCondition.exactTexts;
import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static feature.test.TodoMVCTest.TaskType.ACTIVE;
import static feature.test.TodoMVCTest.TaskType.COMPLETED;

/**
 * Created by barocko on 7/25/2016.
 */
public class TodoMVCTest extends BaseTest {

    @BeforeClass
    public static void setTimeout() {
        Configuration.timeout = 20000;
    }


    @Test
    public void testTaskLifeCycle() {
        given();

        add("1");
        toggle("1");
        assertTasksAre("1");

        filterActive();
        assertNoVisibleTasks();
        add("2");
        assertVisibleTasksAre("2");
        toggleAll();
        assertNoVisibleTasks();

        filterCompleted();
        assertVisibleTasksAre("1", "2");
        //reopen
        toggle("2");
        assertVisibleTasksAre("1");
        clearCompleted();
        assertNoVisibleTasks();

        filterAll();
        assertTasksAre("2");
        assertItemsLeft(1);
    }

    @Test
    public void testEditAtAll() {
        //precondition-added tasks
        given();

        add("1", "2");

        edit("1", "1 edited");

        assertTasksAre("1 edited", "2");
        assertItemsLeft(2);
    }

    @Test
    public void testCancelEditAtCompleted() {
        given();

        add("1", "2");
        toggleAll();
        filterCompleted();

        cancelEdit("2", "to be canceled");

        assertTasksAre("1", "2");
        assertItemsLeft(0);
    }

    @Test
    public void testDeleteAtActive() {
        //precondition-added tasks
        given();

        add("1", "2");
        filterActive();

        delete("1");

        assertTasksAre("2");
        assertItemsLeft(1);
    }

    @Test
    public void deleteAtAll() {
        givenAtAll(COMPLETED, "1");

        delete("1");

        assertNoTasks();
    }

    @Test
    public void testClearCompletedAtAll() {
        givenAtAll(COMPLETED, "2");

        clearCompleted();

        assertNoTasks();
    }

    @Test
    public void testSwitchFilterToCompletedAtAll() {
        given(aTask(COMPLETED, "1"), aTask(ACTIVE, "2"));

        filterCompleted();

        assertVisibleTasksAre("1");
    }

    @Test
    public void testCanceleditAtAll() {
        givenAtAll(ACTIVE, "1");

        cancelEdit("1", "1 editing");

        assertTasksAre("1");
        assertItemsLeft(1);
    }

    @Test
    public void testConfirmEditByPressTabAtAll() {
        givenAtAll(COMPLETED, "1");

        confirmEditByPressTab("1", "1 edited");

        assertTasksAre("1 edited");
        assertItemsLeft(0);
    }

    @Test
    public void testCompleteAtCtive() {
        givenAtActive(ACTIVE, "1", "2");

        toggle("2");

        assertVisibleTasksAre("1");
        assertItemsLeft(1);
    }

    @Test
    public void testEditAtActive() {
        givenAtActive(ACTIVE, "1", "2");

        edit("2", "2 edited");

        assertTasksAre("1", "2 edited");
    }

    @Test
    public void testDeleteByemptyingTextAtActive() {
        givenAtActive(ACTIVE, "1", "2");

        edit("2", "");

        assertTasksAre("1");
        assertItemsLeft(1);
    }

    @Test
    public void testSwitchFilterToAllAtActive() {
        givenAtActive(ACTIVE, "1", "2");
        toggle("2");

        filterAll();

        assertTasksAre("1", "2");
        assertItemsLeft(1);
    }

    @Test
    public void testCancelEditByPressEscapeAtActive() {
        givenAtActive(ACTIVE, "1", "2");

        cancelEdit("2", "2 edited");

        assertTasksAre("1", "2");
        assertItemsLeft(2);
    }

    @Test
    public void testEditAtCompleted() {
        givenAtCompleted(aTask(COMPLETED, "1"), aTask(ACTIVE, "2"));

        edit("1", "1 edited");

        assertVisibleTasksAre("1 edited");
        assertItemsLeft(1);
    }

    @Test
    public void testConifrmEditByClickOutsideAtCompleted() {
        givenAtCompleted(aTask(COMPLETED, "1"), aTask(ACTIVE, "2"));

        confirmEditByClickingOutside("1", "1 edited");

        assertVisibleTasksAre("1 edited");
    }

    @Test
    public void testDeleteAtCompleted() {
        givenAtCompleted(aTask(COMPLETED, "1"), aTask(ACTIVE, "2"));

        delete("1");

        assertNoVisibleTasks();
        assertItemsLeft(1);
    }

    @Test
    public void testSwitchToActiveFilterAtCompleted() {
        givenAtCompleted(aTask(COMPLETED, "1"), aTask(ACTIVE, "2"));

        filterActive();

        assertVisibleTasksAre("2");
        assertItemsLeft(1);
    }

    @Test
    public void testReopenAllAtCompleted() {
        givenAtCompleted(COMPLETED, "1", "2");

        toggleAll();

        assertNoVisibleTasks();
        assertItemsLeft(2);
    }

    ElementsCollection tasks = $$("#todo-list li");

    @Step
    private void clearCompleted() {
        $("#clear-completed").click();
    }

    @Step
    private void add(String... taskTexts) {
        for (String text : taskTexts) {
            $("#new-todo").setValue(text).pressEnter();
        }
    }


    private void delete(String taskText) {
        tasks.find(exactText(taskText)).hover().$(".destroy").click();
    }

    private void toggle(String taskText) {
        tasks.find(exactText(taskText)).$(".toggle").click();
    }

    private void toggleAll() {
        $("#toggle-all").click();
    }

    private void assertTasksAre(String... taskTexts) {
        tasks.shouldHave(exactTexts(taskTexts));
    }

    private void assertNoTasks() {
        tasks.shouldBe(empty);
    }

    private SelenideElement startEdit(String oldTaskText, String newTaskText) {
        tasks.find(exactText(oldTaskText)).doubleClick();
        return tasks.find(cssClass("editing")).find(".edit").setValue(newTaskText);
    }

    private void cancelEdit(String oldTaskText, String newTaskText) {
        startEdit(oldTaskText, newTaskText).pressEscape();
    }

    public void confirmEditByPressTab(String oldTaskText, String newTaskText) {
        startEdit(oldTaskText, newTaskText).pressTab();
    }

    public void confirmEditByClickingOutside(String oldTaskText, String newTaskText) {
        startEdit(oldTaskText, newTaskText);
        $("#header>h1").click();
    }

    private void edit(String oldTaskText, String newTaskText) {
        startEdit(oldTaskText, newTaskText).pressEnter();
    }

    private void filterActive() {
        $(By.linkText("Active")).click();
    }

    private void filterCompleted() {
        $(By.linkText("Completed")).click();
    }

    private void filterAll() {
        $(By.linkText("All")).click();
    }

    private void assertItemsLeft(Integer count) {
        $("#todo-count>strong").shouldHave(exactText((count.toString())));
    }

    private void assertNoVisibleTasks() {
        tasks.filter(visible).shouldBe(empty);
    }

    private void assertVisibleTasksAre(String... taskTexts) {
        tasks.filter(visible).shouldHave(exactTexts(taskTexts));
    }

    public String url() {
        return "url";
    }

    public void ensureApplsOpened() {
        if (!(url().equals("https://todomvc4tasj.herokuapp.com"))) {
            open("https://todomvc4tasj.herokuapp.com");
        }
    }

    public void given(Task... tasks) {
        ensureApplsOpened();
        List<String> taskList = new ArrayList<>();
        for (Task task : tasks) {
            taskList.add("{" + task.taskType + ",\\\"title\\\":\\\"" + task.taskText + "\\\"}");
        }
        executeJavaScript("localStorage.setItem(\"todos-troopjs\",\"[" + String.join(",", taskList) + "]\")");
        refresh();
    }

    public void givenAtActive(Task... tasks) {
        given(tasks);
        filterActive();
    }

    public void givenAtCompleted(Task... tasks) {
        given(tasks);
        filterCompleted();
    }

    public static Task[] aTasks(TaskType taskType, String... taskNames) {
        Task[] tasks = new Task[taskNames.length];
        for (int i = 0; i < taskNames.length; i++) {
            tasks[i] = new Task(taskType, taskNames[i]);
        }
        return tasks;
    }

    public static Task aTask(TaskType taskType, String taskText) {
        return new Task(taskType, taskText);
    }

    public void givenAtActive(TaskType taskType, String... taskNames) {
        givenAtActive(aTasks(taskType, taskNames));
    }

    public void givenAtAll(TaskType taskType, String... taskNames) {
        given(aTasks(taskType, taskNames));
    }

    public void givenAtCompleted(TaskType taskType, String... taskNames) {
        givenAtCompleted(aTasks(taskType, taskNames));
    }

    public static class Task {
        private String taskText;
        private TaskType taskType;

        public Task(TaskType taskType, String taskText) {
            this.taskText = taskText;
            this.taskType = taskType;
        }

        @Override
        public String toString() {
            return "{" + taskType + ",'title':'" + taskText + "'}";
        }
    }

    public enum TaskType {
        ACTIVE("\\\"completed\\\":false"), COMPLETED("\\\"completed\\\":true");

        private String taskStatus;

        @Override
        public String toString() {
            return taskStatus;

        }

        TaskType(String taskStatus) {
            this.taskStatus = taskStatus;
        }


    }

//    public enum Filter {
//        ACTIVE("/#/all"), COMPLETED("/#/completed"), ALL("/#");
//
//        String s;
//
//        Filter(String s) {
//            this.s = s;
//        }
//        public String link() {
//            return "https://todomvc4tasj.herokuapp.com" + s;
//        }
//
//    }
}