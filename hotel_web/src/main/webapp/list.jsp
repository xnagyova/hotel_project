<%--
  Created by IntelliJ IDEA.
  User: kkata
  Date: 03.04.2017
  Time: 22:47
  To change this template use File | Settings | File Templates.
--%>

<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>


<table border="1">
    <thead>
    <tr>
        <th>číslo poschodia</th>
        <th>kapacita</th>
        <th>balkón</th>
    </tr>
    </thead>
    <c:forEach items="${rooms}" var="room">
        <tr>
            <td><c:out value="${room.floorNumber}"/></td>
            <td><c:out value="${room.capacity}"/></td>
            <td><c:out value="${room.balcony}"/></td>
            <td><form method="post" action="${pageContext.request.contextPath}/rooms/delete?id=${room.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Zmazať"></form></td>
        </tr>
    </c:forEach>
</table>

<h2>Zadajte izbu</h2>
<c:if test="${not empty chyba}">
    <div style="border: solid 1px red; background-color: yellow; padding: 10px">
        <c:out value="${chyba}"/>
    </div>
</c:if>
<form action="${pageContext.request.contextPath}/rooms/add" method="post">
    <table>
        <tr>
            <th>číslo poschodia:</th>
            <td><input type="text" name="floorNumber" value="<c:out value='${param.floorNumber}'/>"/></td>
        </tr>
        <tr>
            <th>kapacita:</th>
            <td><input type="text" name="capacity" value="<c:out value='${param.capacity}'/>"/></td>
        </tr>
        <tr>
            <th>balkón:</th>
            <td><input type="text" name="balcony" value="<c:out value='${param.balcony}'/>"/></td>
        </tr>
    </table>
    <input type="Submit" value="Zadať" />
</form>


</body>
</html>

