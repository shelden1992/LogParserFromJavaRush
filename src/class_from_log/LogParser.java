package class_from_log;

import class_from_log.interfaces.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogParser implements IPQuery, UserQuery, DateQuery, EventQuery, QLQuery {
    private Pattern pattern=Pattern.compile("(?<ip>[\\d]+.[\\d]+.[\\d]+.[\\d]+)\\s" +
            "(?<name>[a-zA-Z ]+)" +
            "\\s(?<date>[\\d]+.[\\d]+.[\\d]+ [\\d]+:[\\d]+:[\\d]+)" +
            "\\s(?<event>[\\w]+)" +
            "\\s?((?<taskNumber>[\\d]+)|)" +
            "\\s(?<status>[\\w]+)");
    private SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public List<String> list=new LinkedList<>();
    static List<MyLogParser> myLogParserList=new LinkedList<>();


    public Stream<String> convertToStream() {
        Stream<String> files=null;
        try {
            files=Files.lines(logDir);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return files;
    }


    public LogParser(Path logDir) {


        this.logDir=logDir;
        splitStream();


    }

    private Path logDir;


    public void splitStream() {


        try {

            Files.walk(logDir).filter(s -> s.toString().endsWith(".log"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList()).forEach(str -> {
                try {

                    Files.lines(str.toPath()).forEach(s -> list.add(s));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String st : list
        ) {
            Matcher matcher=pattern.matcher(st);
            while (matcher.find()) {
                if (matcher.group("taskNumber") == null) {
                    try {
                        myLogParserList.add(new MyLogParser(matcher.group("ip"), matcher.group("name"), dateFormat.parse(matcher.group("date")),
                                Event.valueOf(matcher.group("event")), Status.valueOf(matcher.group("status"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        myLogParserList.add(new MyLogParser(matcher.group("ip"), matcher.group("name"), dateFormat.parse(matcher.group("date")),
                                Event.valueOf(matcher.group("event")), Integer.parseInt(matcher.group("taskNumber")), Status.valueOf(matcher.group("status"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }


        }


    }


    @Override
    public int getNumberOfUniqueIPs(Date after, Date before) {

        return (int) myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() )).map(log -> log.getIp())
                .distinct()
                .count();

    }

    @Override
    public Set<String> getUniqueIPs(Date after, Date before) {

        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() )).map(log -> log.getIp())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<String> getIPsForUser(String user, Date after, Date before) {


        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() )).
                filter(log -> log.getUser().equals(user)).map(log -> log.getIp())
                .collect(Collectors.toSet());
    }


    @Override
    public Set<String> getIPsForEvent(Event event, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() )).
                filter(log -> log.getEvent() == ( event )).map(log -> log.getIp())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForStatus(Status status, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() )).
                filter(log -> log.getStatus() == ( status )).map(log -> log.getIp())
                .collect(Collectors.toSet());
    }


    @Override
    public Set<String> getAllUsers() {
        return myLogParserList.stream().map(MyLogParser::getUser).collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfUsers(Date after, Date before) {
        return (int) myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .map(log -> log.getUser()).distinct().count();
    }

    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) { //события количество ВСЕХ

        return (int) myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user)).map(log -> log.getEvent()).distinct().count();
    }

    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {


        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getIp().equals(ip)).map(log -> log.getUser())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<String> getLoggedUsers(Date after, Date before) {

        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent() == ( Event.LOGIN )).map(log -> log.getUser())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDownloadedPluginUsers(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent() == ( Event.DOWNLOAD_PLUGIN )).filter(log -> log.getStatus() == Status.OK).map(log -> log.getUser())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<String> getWroteMessageUsers(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() )).
                filter(log -> log.getEvent() == ( Event.WRITE_MESSAGE )).filter(log -> log.getStatus() == Status.OK).map(log -> log.getUser())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent() == ( Event.SOLVE_TASK )).map(log -> log.getUser())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before, int task) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent() == ( Event.SOLVE_TASK )).filter(log -> log.getTaskNumber() == task).map(log -> log.getUser())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent() == ( Event.DONE_TASK )).map(log -> log.getUser())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before, int task) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent() == ( Event.DONE_TASK )).filter(log -> log.getTaskNumber() == task).map(log -> log.getUser())
                .collect(Collectors.toSet());


    }

    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .filter(log -> log.getEvent() == ( event ))
                .map(MyLogParser::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getStatus() == ( Status.FAILED ))
                .map(MyLogParser::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenErrorHappened(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getStatus() == ( Status.ERROR ))
                .map(MyLogParser::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Date getDateWhenUserLoggedFirstTime(String user, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .filter(log -> log.getEvent() == Event.LOGIN)
                .map(log -> log.getDate())
                .sorted()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Date getDateWhenUserSolvedTask(String user, int task, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .filter(log -> log.getEvent() == Event.SOLVE_TASK)
                .filter(log -> log.getTaskNumber() == task)
                .map(log -> log.getDate())
                .sorted()
                .findFirst()
                .orElse(null);

    }

    @Override
    public Date getDateWhenUserDoneTask(String user, int task, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .filter(log -> log.getEvent() == Event.DONE_TASK)
                .filter(log -> log.getTaskNumber() == task)
                .map(log -> log.getDate())
                .sorted()
                .findFirst()
                .orElse(null);

    }

    @Override
    public Set<Date> getDatesWhenUserWroteMessage(String user, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .filter(log -> log.getEvent() == Event.WRITE_MESSAGE)
                .map(log -> log.getDate())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .filter(log -> log.getEvent() == Event.DOWNLOAD_PLUGIN)
                .map(log -> log.getDate())
                .collect(Collectors.toSet());

    }

    ///////////////////////////////////////////////////////////////////////////////
    @Override
    public int getNumberOfAllEvents(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .map(log -> log.getEvent())
                .collect(Collectors.toSet()).size();


    }

    @Override
    public Set<Event> getAllEvents(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .map(log -> log.getEvent())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<Event> getEventsForIP(String ip, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getIp().equals(ip))
                .map(log -> log.getEvent())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<Event> getEventsForUser(String user, Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getUser().equals(user))
                .map(log -> log.getEvent())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<Event> getFailedEvents(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getStatus() == Status.FAILED)
                .map(log -> log.getEvent())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<Event> getErrorEvents(Date after, Date before) {
        return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getStatus() == Status.ERROR)
                .map(log -> log.getEvent())
                .collect(Collectors.toSet());


    }

    @Override
    public int getNumberOfAttemptToSolveTask(int task, Date after, Date before) { //проблема в том, что не знаю по чем фильтровать по ЕРРОРУ или По ФАЙЛДУ???
//        int i=0;
//        for (MyLogParser record : myLogParserList) {
//            if (isDateInside(after, before, record.getDate())
//                    && record.getEvent().equals(class_from_log.Event.SOLVE_TASK)
//                    && record.getTaskNumber() == task) {
//                i++;
//            }
//        }
//        return i;
//

        if (task == 0) {
            return 0;
        } else
            return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                    .filter(log -> log.getEvent() == Event.SOLVE_TASK)

                    .filter(log -> !String.valueOf(log.getTaskNumber()).isEmpty() && String.valueOf(log.getTaskNumber()).equals(String.valueOf(task)))
                    .map(log -> log.getTaskNumber())
                    .collect(Collectors.toList()).size();

    }

    ////////////////////если это ебанько зароботает (точнее пройдет валидатор), то це буде жесть /////////////////////
    private boolean isDateInside(Date after, Date before, Date currentDate) {
        if (after != null) {
            if (currentDate.getTime() <= after.getTime())
                return false;
        }
        if (before != null) {
            if (currentDate.getTime() >= before.getTime())
                return false;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int getNumberOfSuccessfulAttemptToSolveTask(int task, Date after, Date before) {

//        int i=0;
//        for (MyLogParser record : myLogParserList) {
//            if (isDateInside(after, before, record.getDate())
//                    && record.getEvent().equals(class_from_log.Event.DONE_TASK)
//                    && record.getTaskNumber() == task) {
//                i++;
//            }
//        }
//        return i;
//
//
        if (task == 0) {
            return 0;
        } else
            return myLogParserList.stream().filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                    .filter(log -> log.getEvent() == Event.DONE_TASK)

                    .filter(log -> !String.valueOf(log.getTaskNumber()).isEmpty() && String.valueOf(log.getTaskNumber()).equals(String.valueOf(task)))
                    .filter(log -> log.getTaskNumber() != 0)
                    .map(log -> log.getTaskNumber())
                    .collect(Collectors.toList()).size();

    }

    @Override
    public Map<Integer, Integer> getAllSolvedTasksAndTheirNumber(Date after, Date before) { //numberTask == solve_task

//        Map<Integer, Integer> taskSolved=new HashMap<>();
//        for (MyLogParser record : myLogParserList) {
//            if (isDateInside(after, before, record.getDate()) && record.getEvent().equals(class_from_log.Event.SOLVE_TASK)) {
//                int task=record.getTaskNumber();
//                if (taskSolved.containsKey(task)) {
//                    taskSolved.put(task, taskSolved.get(task) + 1);
//                } else {
//                    taskSolved.put(task, 1);
//                }
//            }
//        }
//        return taskSolved;
//
//
        HashMap<Integer, Integer> map=new HashMap<>();
        myLogParserList.stream()
                .filter(log -> ( after == null || ( log.getDate().getTime() >= after.getTime() ) ) && ( before == null || log.getDate().getTime() <= before.getTime() ))
                .filter(log -> log.getEvent().equals(Event.SOLVE_TASK))
                .filter(log -> log.getTaskNumber() != 0)
                .forEach(log -> map.put(log.getTaskNumber(), map.getOrDefault(log.getTaskNumber(), 0) + 1));

        return map;
    }

    @Override
    public Map<Integer, Integer> getAllDoneTasksAndTheirNumber(Date after, Date before) {
//        номер_задачи :сколько_раз_ее_решили).

//        Map<Integer, Integer> taskSolved=new HashMap<>();
//        for (MyLogParser record : myLogParserList) {
//            if (isDateInside(after, before, record.getDate()) && record.getEvent().equals(class_from_log.Event.DONE_TASK)) {
//                int task=record.getTaskNumber();
//                if (taskSolved.containsKey(task)) {
//                    taskSolved.put(task, taskSolved.get(task) + 1);
//                } else {
//                    taskSolved.put(task, 1);
//                }
//            }
//        }
//        return taskSolved;
//
//
        Map<Integer, Integer> result=new HashMap<>();
        myLogParserList.stream()
                .filter(log -> log.getEvent() == Event.DONE_TASK && ( after == null || log.getDate().after(after) ) && ( before == null || log.getDate().before(before) ))
                .filter(log -> log.getTaskNumber() != 0)
//                .map(log -> log.getTaskNumber())
                .forEach(log -> result.put(log.getTaskNumber(), result.get(log.getTaskNumber()) == null ? 1 : result.get(log.getTaskNumber()) + 1));
        return result;
    }

    @Override
    public Set<Object> execute(String query) {
        Pattern pattern=Pattern.compile("([get]+)\\s(?<field1>[a-zA-Z\\d]+)\\s([for]+)\\s(?<field2>[a-zA-Z \\d]+)\\s[=]\\s[\\\"](?<value1>[\\w\\d.\\s:]+)[\\\"](\\s[and date between]+\\s[\\\"](?<after>[\\d.\\s:]+)[\\\"]\\s[and]+\\s[\\\"](?<before>[\\d.\\s:]+)[\\\"].)?");
        Matcher matcher=pattern.matcher(query);
        List<String> list=new LinkedList<>();


        while (matcher.find()) {

            Date dateAfter=null;
            Date dateBefore=null;
            try {
                if (matcher.group("after") != null) {
                    dateAfter=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(matcher.group("after"));
                }
                if (matcher.group("before") != null) {
                    dateBefore=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(matcher.group("before"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            switch (matcher.group("field2")) {
                case "ip": // 2-й параметр
                    if (dateAfter != null && dateBefore != null) {

                        if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                                .contains(( matcher.group("value1") ))) {
                            Date finalDateAfter=dateAfter;
                            Date finalDateBefore=dateBefore;
                            return myLogParserList.stream()
                                    .filter(log -> log.getDate().getTime() >= finalDateAfter.getTime() && log.getDate().getTime() <= finalDateBefore.getTime())
                                    .filter(log -> log.gateEverything(matcher.group("field2")).equals(( matcher.group("value1") )))
                                    .map(log -> log.gateEverything(matcher.group("field1")))
                                    .collect(Collectors.toSet());

                        }
                    } else if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                            .contains(( matcher.group("value1") ))) {
                        return myLogParserList.stream().
                                filter(log -> log.gateEverything(matcher.group("field2")).equals(( matcher.group("value1") )))
                                .map(log -> log.gateEverything(matcher.group("field1")))
                                .collect(Collectors.toSet());
                    } else break;

                case "event":


                    if (dateAfter != null && dateBefore != null) {

                        if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                                .contains(( matcher.group("value1") ))) {
                            Date finalDateAfter=dateAfter;
                            Date finalDateBefore=dateBefore;
                            return myLogParserList.stream()
                                    .filter(log -> log.getDate().getTime() >= finalDateAfter.getTime() && log.getDate().getTime() <= finalDateBefore.getTime())
                                    .filter(log -> log.gateEverything(matcher.group("field2")).equals(class_from_log.Event.valueOf(matcher.group("value1"))))
                                    .map(log -> log.gateEverything(matcher.group("field1")))
                                    .collect(Collectors.toSet());

                        }
                    } else if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                            .contains(class_from_log.Event.valueOf(matcher.group("value1")))) {
                        return myLogParserList.stream().
                                filter(log -> log.gateEverything(matcher.group("field2")).equals(class_from_log.Event.valueOf(matcher.group("value1"))))
                                .map(log -> log.gateEverything(matcher.group("field1")))
                                .collect(Collectors.toSet());
                    } else break;


                case "date":
                    Date date=null;


                    try {
                        date=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(matcher.group("value1"));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    if (dateAfter != null && dateBefore != null) {

                        if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                                .contains(( matcher.group("value1") ))) {
                            Date finalDate=date;
                            Date finalDateAfter=dateAfter;
                            Date finalDateBefore=dateBefore;
                            return myLogParserList.stream()
                                    .filter(log -> log.getDate().getTime() >= finalDateAfter.getTime() && log.getDate().getTime() <= finalDateBefore.getTime())
                                    .filter(log -> log.gateEverything(matcher.group("field2")).equals(finalDate))
                                    .map(log -> log.gateEverything(matcher.group("field1")))
                                    .collect(Collectors.toSet());

                        }
                    } else if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                            .contains(date)) {
                        Date finalDate=date;
                        return myLogParserList.stream()
                                .filter(log -> log.gateEverything(matcher.group("field2")).equals(finalDate))
                                .map(log -> log.gateEverything(matcher.group("field1")))
                                .collect(Collectors.toSet());
                    } else break;
                case "user":

                    if (dateAfter != null && dateBefore != null) {
                        if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                                .contains(( matcher.group("value1") ))) {
                            Date finalDateAfter=dateAfter;
                            Date finalDateBefore=dateBefore;
                            return myLogParserList.stream()
                                    .filter(log -> log.getDate().getTime() >= finalDateAfter.getTime() && log.getDate().getTime() <= finalDateBefore.getTime())
                                    .filter(log -> log.gateEverything(matcher.group("field2")).equals(matcher.group("value1")))
                                    .map(log -> log.gateEverything(matcher.group("field1")))
                                    .collect(Collectors.toSet());

                        }
                    } else if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                            .contains(( matcher.group("value1") ))) {
                        return myLogParserList.stream()
                                .filter(log -> log.gateEverything(matcher.group("field2")).equals(matcher.group("value1")))
                                .map(log -> log.gateEverything(matcher.group("field1")))
                                .collect(Collectors.toSet());
                    } else break;


                case "status":


                    if (dateAfter != null && dateBefore != null) {
                        if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                                .contains(( matcher.group("value1") ))) {
                            Date finalDateAfter=dateAfter;
                            Date finalDateBefore=dateBefore;
                            return myLogParserList.stream()
                                    .filter(log -> log.getDate().getTime() >= finalDateAfter.getTime() && log.getDate().getTime() <= finalDateBefore.getTime())
                                    .filter(log -> log.gateEverything(matcher.group("field2")).equals(class_from_log.Status.valueOf(matcher.group("value1"))))
                                    .map(log -> log.gateEverything(matcher.group("field1")))
                                    .collect(Collectors.toSet());

                        }
                    } else if (myLogParserList.stream().map(log -> log.gateEverything(matcher.group("field2"))).collect(Collectors.toSet())
                            .contains(class_from_log.Status.valueOf(matcher.group("value1")))) {
                        return myLogParserList.stream()
                                .filter(log -> log.gateEverything(matcher.group("field2")).equals(class_from_log.Status.valueOf(matcher.group("value1"))))
                                .map(log -> log.gateEverything(matcher.group("field1")))
                                .collect(Collectors.toSet());
                    } else break;


            }

        }


        switch (query) {
            case "get ip":
                return myLogParserList.stream().map(log -> log.getIp()).collect(Collectors.toSet());

            case "get user":
                return myLogParserList.stream().map(log -> log.getUser()).collect(Collectors.toSet());
            case "get date":
                return myLogParserList.stream().map(log -> log.getDate()).collect(Collectors.toSet());
            case "get event":
                return myLogParserList.stream().map(log -> log.getEvent()).collect(Collectors.toSet());
            case "get status":
                return myLogParserList.stream().map(log -> log.getStatus()).collect(Collectors.toSet());

        }


        return null;
    }

}
