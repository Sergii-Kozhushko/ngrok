package de.hellfish.ngrok.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest

public class Test2 {

    @Autowired
    private ServerRunner serverRunner;

    @Autowired
    ApplicationContext ctx;

    @Test
    public void serverRunnerStartedTest(CapturedOutput capturedOutput) throws InterruptedException {
        ServerRunner runner = ctx.getBean(ServerRunner.class);
        runner.stopServer();

        assertEquals(true, runner.isFlag());

        // Change the value of myVariable
//        serverRunner.setFlag(false);
//
//        // Now, myVariable should be true
//        assertEquals(false, serverRunner.isFlag());
//        serverRunner.setFlag(false);
//        serverRunner.run();
//
//        assertThat(capturedOutput).contains("Server started");
    }
}