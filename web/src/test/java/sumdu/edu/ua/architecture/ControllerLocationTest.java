package sumdu.edu.ua.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ControllerLocationTest {

    @Test
    void controllersShouldResideOnlyInWeb() {
        JavaClasses imported = new ClassFileImporter()
                .importPackages("sumdu.edu.ua");

        classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..web..")
                .check(imported);
    }

    @Test
    void servletsShouldResideOnlyInWeb() {
        JavaClasses imported = new ClassFileImporter()
                .importPackages("sumdu.edu.ua");

        classes()
                .that().haveSimpleNameEndingWith("Servlet")
                .should().resideInAPackage("..web..")
                .allowEmptyShould(true)
                .check(imported);
    }
}
