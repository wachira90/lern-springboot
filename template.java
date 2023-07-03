package com.getstart.demogetstart;
import org.springframework.boot.SpringApplication; 
import org.springframework.boot.autoconfigure.SpringBootApplication;

//add new
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//add new

@SpringBootApplication

//add new
@RestController
//add new
public class DemogetstartApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemogetstartApplication.class, args);
	}
//	add new	
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
      return String.format("Hello %s!", name);
    }
//add new
}
