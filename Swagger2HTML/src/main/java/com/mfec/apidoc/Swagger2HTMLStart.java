package com.mfec.apidoc;

import static java.lang.System.exit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import com.mfec.apidoc.service.SwaggerDocumentService;

@SpringBootApplication
public class Swagger2HTMLStart implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(Swagger2HTMLStart.class);
	
    @Autowired
    private SwaggerDocumentService swaggerDocumentService;

    public static void main(String[] args) throws Exception {
    	//SpringApplication.run(SpringBootConsoleApplication.class, args);

        SpringApplication app = new SpringApplication(Swagger2HTMLStart.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);

    }

	@Override
	public void run(String... args) throws Exception {
		logger.debug(">>> START : Swagger2HTMLStart.run >>>");
		
		if (args != null && args.length > 0 && !StringUtils.isEmpty(args[0])) {
			logger.info(" ##### Run Swagger2HTMLStart.run : Run with argument [" + args[0] + "] #####");
			swaggerDocumentService.generateByName(args[0]);
		} else {
			logger.info(" ##### Run Swagger2HTMLStart.run : Run without argument #####");
			swaggerDocumentService.generateAll();
		}

		logger.debug("<<< END : Swagger2HTMLStart.run <<<");
		exit(0);
	}
}