
try(Connection con = ds.getConnection()) {
for (String line : Files.readAllLines(Paths.get("src", "main", "resources", "data.sql"))) {
if(line.trim().isEmpty()) continue;
if(line.endsWith(";")) line=line.substring(0,line.length()-1);
log.debug("executing \"{}\"",line);
try (PreparedStatement st1 = con.prepareStatement(line)) {
st1.execute();
}
}
}