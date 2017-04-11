<%--
  Created by IntelliJ IDEA.
  User: kkatanik a snagyova
  Date: 03.04.2017
  Time: 22:53
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="cz.muni.fi.web.RoomsServlet" %>
<%@ page import="cz.muni.fi.web.GuestsServlet" %>
<%@ page import="cz.muni.fi.web.BookingsServlet" %>
<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--<c:redirect url="<%=RoomsServlet.URL_MAPPING%>"/>--%>
<%--<c:redirect url="<%=GuestsServlet.URL_MAPPING%>"/>--%>
<html>
<head>
    <title>JSP forward action tag example</title>
</head>
<body>
    <h1>Izby</h1><form action="<%=RoomsServlet.URL_MAPPING%>">
        <input type="submit" value="Vybrať"/>
    </form>
    <h1>Hostia</h1><form action="<%=GuestsServlet.URL_MAPPING%>">
        <input type="submit" value="Vybrať"/>
    </form>
    <h1>Rezervácie</h1><form action="<%=BookingsServlet.URL_MAPPING%>">
        <input type="submit" value="Vybrať"/>
    </form>
</body>
</html>

