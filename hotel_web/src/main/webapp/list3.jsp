<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>


<table border="1">
    <thead>
    <tr>
        <th>cena</th>
        <th>izba</th>
        <th>hosť</th>
        <th>dátum príchodu</th>
        <th>dátum odchodu</th>
    </tr>
    </thead>
    <c:forEach items="${bookings}" var="booking">
        <tr>
            <td><c:out value="${booking.price}"/></td>
            <td><c:out value="${booking.room}"/></td>
            <td><c:out value="${booking.guest}"/></td>
            <td><c:out value="${booking.arrivalDate}"/></td>
            <td><c:out value="${booking.departureDate}"/></td>
            <td><form method="post" action="${pageContext.request.contextPath}/bookings/delete?id=${booking.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Zmazať"></form></td>
            <td><form method="post" action="${pageContext.request.contextPath}/bookings/update?id=${booking.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Editovať"></form></td>
        </tr>
    </c:forEach>
</table>


<h2>Zadajte rezerváciu</h2>
<c:if test="${not empty chyba}">
    <div style="border: solid 1px red; background-color: yellow; padding: 10px">
        <c:out value="${chyba}"/>
    </div>
</c:if>
<form action="${pageContext.request.contextPath}/bookings/add" method="post">
    <table>
        <tr>
            <th>cena:</th>
            <td><input type="text" name="price" value="<c:out value='${param.price}'/>"/></td>
        </tr>
        <tr>
            <th>ID izby:</th>
            <td><input type="text" name="roomId" value="<c:out value='${param.roomId}'/>"/></td>
        </tr>
        <tr>
            <th>ID hosťa:</th>
            <td><input type="text" name="guestId" value="<c:out value='${param.guestId}'/>"/></td>
        </tr>
        <tr>
            <th>dátum príchodu(yyyy-mm-dd):</th>
            <td><input type="text" name="arrivalDate" value="<c:out value='${param.arrivalDate}'/>"/></td>
        </tr>
        <tr>
            <th>dátum odchodu(yyyy-mm-dd):</th>
            <td><input type="text" name="departureDate" value="<c:out value='${param.departureDate}'/>"/></td>
        </tr>
    </table>
    <input type="Submit" value="Zadať" />
</form>


</body>