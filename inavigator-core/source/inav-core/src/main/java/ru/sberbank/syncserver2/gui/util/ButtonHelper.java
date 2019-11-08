package ru.sberbank.syncserver2.gui.util;

import java.io.File;

public class ButtonHelper {
    public static String showButton(String title, String href, String normalImage) {
        return "<img style=\"cursor: pointer; \" class=\"transparent\" src=\"../images/" + normalImage + "\" " +
               "width=50 height=25 " +
        	   "alt=\"" + title + "\" title=\"" + title + "\" " +
               "style=\"cursor:hand;\" " +
        	   "onclick=\"javascript:window.location='" + href + "';\"/>";
    }

    public static String showBottomButton(String title, String href, String normalImage) {
        return "<img style=\"cursor: pointer; \" class=\"transparent\" src=\"../images/" + normalImage + "\" " +
        	   "alt=\"" + title + "\" title=\"" + title + "\" " +
               "style=\"cursor:hand;\" " +
        	   "onclick=\"javascript:window.location='" + href + "';\"/>";
    }

    public static String showButton(String title, String href, String normalImage, String mouseOverImage){
        return "<img style=\"cursor: pointer; \" src=\"../images/"+normalImage+"\" " +
               "alt=\""+title+"\" title=\""+title+"\" " +
               "style=\"cursor:hand;\" " +
        	   "onmouseover=\"javascript:this.src = '../images/"+mouseOverImage+"'\" " +
		       "onmouseout=\"javascript:this.src = '../images/"+normalImage+"'\" " +
		       "onclick=\"javascript:window.location='"+href+"';\"/>";
    }

    public static String showDeleteButton(String title, String confirmText, String href, String normalImage, String mouseOverImage){
        return "<img style=\"cursor: pointer; \" src=\"../images/"+normalImage+"\" " +
               "alt=\""+title+"\" title=\""+title+"\" " +
               "style=\"cursor:hand;\" " +
               "onmouseover=\"javascript:this.src = '../images/"+mouseOverImage+"'\" " +
               "onmouseout=\"javascript:this.src = '../images/"+normalImage+"'\" " +
               "onclick=\"javascript:showConfirm('"+confirmText+"','"+href+"');\"/>";
    }

    public static String showDisabledButton(String title, String normalImage, String mouseOverImage){
        return "<img style=\"cursor: pointer; \" src=\"../images/"+normalImage+"\" " +
                "alt=\""+title+"\" title=\""+title+"\" " +
                "style=\"cursor:hand;\" " +
                "onmouseover=\"javascript:this.src = '../images/"+mouseOverImage+"'\" " +
                "onmouseout=\"javascript:this.src = '../images/"+normalImage+"'\"/>";
    }

    public static String showSubmit(String title, String formName, String normalImage){
        return "<img style=\"cursor: pointer; \" src=\"../images/"+normalImage+"\" " +
                "alt=\""+title+"\" title=\""+title+"\" " +
                "style=\"cursor:hand;\" " +
                "onclick=\"javascript:"+formName+".submit();\"/>";
    }

    public static String showSave(String formName){
        return "<input type=\"submit\" value=\"Сохранить\">";
//        return showSubmit("Сохранить",formName, "buttons/i-save.png ");
    }

    public static String showJavaScriptButton(String title, String javascript, String normalImage){
        return "<img style=\"cursor: pointer; \" src=\"../images/"+normalImage+"\" " +
               "alt=\""+title+"\" title=\""+title+"\" " +
               "style=\"cursor:hand;\" " +
               "onclick=\""+javascript+"\"/>";
    }

/*
    public static String showJavaScriptButton(String title, String javascript, String normalImage, String mouseOverImage){
        if(mouseOverImage==null){
            mouseOverImage = normalImage;
        }
        return "<img src=\"../images/"+normalImage+"\" " +
               "alt=\""+title+"\" title=\""+title+"\" " +
               "style=\"cursor:hand;\" " +
               "onmouseover=\"javascript:this.src = '../images/"+mouseOverImage+"'\" " +
               "onmouseout=\"javascript:this.src = '../images/"+normalImage+"'\" " +
               "onclick=\""+javascript+"\"/>";
    } */

    private static File EDITOR_FILE = new File("C:/usr/tomcat7/webapps/vito/editor/");

    public static String showMissingButtonInLine(String title, String javaScript, String normalImage){
        if(new File(EDITOR_FILE,normalImage).exists()){
            return showButton(title,javaScript, normalImage);
        } else {
            return showMissingButton(title,javaScript,normalImage);
        }
    }

    public static String showMissingButtonAtBottom(String title, String javaScript, String normalImage){
        if(new File(EDITOR_FILE,normalImage).exists()){
            return showBottomButton(title,javaScript, normalImage);
        } else {
            return showMissingButton(title,javaScript,normalImage);
        }
    }

    public static String showMissingSubmitAtBottom(String title, String formName, String normalImage){
        if(new File(EDITOR_FILE,normalImage).exists()){
            return showSubmit(title,formName, normalImage);
        } else {
            return showMissingJavaScriptAtBottom(title,"javascript:"+formName+".submit();",normalImage);
        }
    }

    public static String showMissingJavaScriptAtBottom(String title, String javaScript, String normalImage){
        if(new File(EDITOR_FILE,normalImage).exists()){
            return showJavaScriptButton(title,javaScript, normalImage);
        } else {
            return showMissingButton(title,javaScript,normalImage);
        }
    }

    private static String showMissingButton(String title, String javaScript, String normalImage){
        return showMissingJavaScriptButton(title,"javascript:window.location='"+javaScript+"';", normalImage);
    }

    private static String showMissingJavaScriptButton(String title, String javaScript, String normalImage){
        return "<input type=button value=\"" + title + "\" " +
                "onclick=\""+javaScript+"\" >";
//        return "<img src=\"../images/buttons/missing_button.png\" " +
//               "alt=\"Нет кнопки "+title+"\" title=\"Нет кнопки "+normalImage+" для "+title+"\" " +
//               "style=\"cursor:hand;\" " +
//               "onclick=\""+javaScript+"\"/>";
    }

    public static String showMissingImage(String title, String normalImage){
        if(new File(EDITOR_FILE,normalImage).exists()){
            return "<img src=\"../images/buttons/"+normalImage+"\" " +
                   "alt=\"Нет рисунка "+title+"\" title=\"Нет кнопки "+normalImage+" для "+title+"\" />";
        } else {
            return "<img src=\"../images/buttons/missing_button.png\" " +
               "alt=\"Нет рисунка "+title+"\" title=\"Нет кнопки "+normalImage+" для "+title+"\" />";
        }
    }

}