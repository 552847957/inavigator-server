package ru.sberbank.syncserver2.gui.web;

import org.springframework.web.servlet.ModelAndView;

/**
 * Created by sbt-kozhinsky-lb on 28.03.14.
 */
public class ShowHtmlController extends DatabaseController {
    public ShowHtmlController(Class loggerClass) {
        super(loggerClass);
    }

    protected ModelAndView showText(String text){
        ModelAndView mv = new ModelAndView("html");
        mv.addObject("html"  , text);
        return mv;
    }

    protected ModelAndView showTable(String text){
        text = "<table border=1>"+text+"</table>";
        ModelAndView mv = new ModelAndView("html");
        mv.addObject("html"  , text);
        return mv;
    }

}
