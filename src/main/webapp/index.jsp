<%@ page import="java.util.*, org.dew.cms.*" %>
<%
  List<List<Object>> categories = CMS.getCategories();
  
  List<Article> articles = null;
  
  int category = WU.getCategoryPar(request);
  if(category != 0) {
    // Find article by category
    articles = CMS.find(category, 0);
  }
  else {
    articles = new ArrayList<Article>();
  }
%>
<!DOCTYPE html>
<html>
  <head>
    <title>CMS</title>
  </head>
  <body>
    <h3>CMS</h3>
    <hr>
    <h4>Categories:</h4>
    <br>
    <% 
      for(int i = 0; i < categories.size(); i++) {
        List<Object> item = categories.get(i);
        out.println("<a href=\"index.jsp?" + CMS.PAR_CATEGORY + "=" + item.get(0) + "\">" + item.get(1) + "</a><br />");
      }
    %>
    <hr>
    <h4>Articles by category (<%= category %>):</h4>
    <br>
    <% 
      for(int i = 0; i < articles.size(); i++) {
        Article article = articles.get(i);
        out.println(article.getTitle() + "<br />");
      }
    %>
  </body>
</html>