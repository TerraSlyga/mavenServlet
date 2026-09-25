package sumdu.edu.ua.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class PersistenceArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("sumdu.edu.ua.persistence");

    @Test
    void persistenceShouldNotDependOnWeb() {
        noClasses().that().resideInAPackage("..persistence..")
                .should().dependOnClassesThat().resideInAnyPackage("..web..", "jakarta.servlet..", "javax.servlet..")
                .check(classes);
    }

    @Test
    void repositoryImplementationsShouldHaveProperNaming() {
        classes().that().resideInAPackage("..persistence..")
                .and().haveSimpleNameEndingWith("RepositoryImpl")
                .or().haveSimpleNameEndingWith("DaoImpl")
                .should().bePublic()
                .allowEmptyShould(true)
                .check(classes);
    }
}
