package de.otto.synapse.edison.history;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class HistoryConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setup() {
        context = new AnnotationConfigApplicationContext();
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void shouldNotExposeHistoryByDefault() {
        context.register(HistoryConfiguration.class);
        context.refresh();

        assertThat(context.containsBean("historyController"), is(false));
        assertThat(context.containsBean("historyService"), is(false));
    }

    @Test
    public void shouldNotExposeHistoryIfDisabled() {
        addEnvironment(this.context,
                "synapse.edison.history.enabled=false"
        );
        context.register(HistoryConfiguration.class);
        context.refresh();

        assertThat(context.containsBean("historyController"), is(false));
        assertThat(context.containsBean("historyService"), is(false));
    }

    @Test
    public void shouldExposeHistoryIfEnabled() {
        addEnvironment(this.context,
                "synapse.edison.history.enabled=true"
        );
        context.register(HistoryConfiguration.class);
        context.refresh();

        assertThat(context.containsBean("historyController"), is(true));
        assertThat(context.containsBean("historyService"), is(true));
    }
}