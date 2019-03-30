package class_from_log;

import java.util.Date;
import java.util.List;

    public class MyLogParser {
        private String ip;
        private String user;
        private Date date;
        private Status status;
        private Event event;
        private int taskNumber;
        List<String> list;

        public String getIp() {
            return ip;
        }

        public int getTaskNumber() {
            return taskNumber;
        }

        public void setIp(String ip) {
            this.ip=ip;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user=user;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date=date;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status=status;
        }

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event=event;
        }

        public MyLogParser() {
            this.taskNumber=0;
        }

        public MyLogParser(String ip, String user, Date date, Event event, int taskNumber, Status status) {
            this.ip=ip;
            this.user=user;
            this.date=date;
            this.status=status;
            this.event=event;
            this.taskNumber=taskNumber;
        }

        @Override
        public String toString() {
            final StringBuilder sb=new StringBuilder("MyLogParser{");
            sb.append("ip='").append(ip).append('\'');
            sb.append(", user='").append(user).append('\'');
            sb.append(", date=").append(date);
            sb.append(", status=").append(status);
            sb.append(", event=").append(event);
            sb.append(", taskNumber=").append(taskNumber);
            sb.append('}');
            return sb.toString();
        }

        public MyLogParser(String ip, String user, Date date, Event event, Status status) {
            this.ip=ip;
            this.user=user;
            this.date=date;
            this.status=status;
            this.event=event;
        }

        public Object gateEverything(String st) {
            if (st.equals("user")) {

                return getUser();
            } else if (st.equals("date") ) {
                return getDate();
            }
            else if (st.equals("status")){
                return getStatus();
            }
            else if (st.equals("event")) {
                return getEvent();

            } else if (st.equals("ip")) {
                return getIp();
            }



            return null;

        }

    }


