import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) {
        int port = 8080;
        try {
            System.out.print("Сервер стартован!");
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            while (true) {
                // приём сообщения
                SocketChannel inChannel = server.accept();
                ByteBuffer buffer = ByteBuffer.allocate(256);
                int byteRead = inChannel.read(buffer);
                String request = new String(buffer.array(), "UTF-8");
                String answer = null;
                System.out.println("Получено сообщение: " + request);

// проверка на содержание "?", "&", "=" в запросе
                if (request.contains("?") && request.contains("&") && request.contains("=")) {
                    HashMap<String, String> paramAndValue = new HashMap<>();
                    request = request.substring(request.indexOf("?") + 1, request.length());
                    String[] data = request.split("&");

                    for (int i = 0; i < data.length; i++) {
                        paramAndValue.put(data[i].substring(0, data[i].indexOf("=")), data[i].substring(data[i].indexOf("=") + 1, data[i].length()));
                    }

                    if (paramAndValue.get("type_of_payment").equals("annuity")) {
                        answer = func_kalc_annuitet(Integer.parseInt(paramAndValue.get("amount_of_credit")) - Integer.parseInt(paramAndValue.get("amount_of_first_payment")),
                                Integer.parseInt(paramAndValue.get("interest_of_credit")), Integer.parseInt(paramAndValue.get("period")), paramAndValue.get("date_of_first_payment"));
                    } else if (paramAndValue.get("type_of_payment").equals("differential")) {
                        answer = func_kalc_dif(Integer.parseInt(paramAndValue.get("amount_of_credit")) - Integer.parseInt(paramAndValue.get("amount_of_first_payment")),
                                Integer.parseInt(paramAndValue.get("interest_of_credit")), Integer.parseInt(paramAndValue.get("period")), paramAndValue.get("date_of_first_payment"));
                    } else {
                        answer = "Ошибка: неверный тип платежа!";
                    }
                }

                // отправка сообщения
                byte[] bt = answer.getBytes("UTF-8");
                buffer = ByteBuffer.wrap(bt);
                inChannel.write(buffer);
                System.out.println("Отправлено сообщение: " + answer);
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    /*
        Функция расчитывает размер месячного платежа для аннуитетного вида
        *@param S - первоначальная сумма кредита
        *@param P - процентная ставка
        *@param N - количество месяцев
        *@param D - дата первого платежа в формате "dd/MM/yyyy"
        *@return строку в формате json
         */
    public static String func_kalc_annuitet(int S, int P, int N, String D) {
        // определение переменных
        double period = N;
        double ostatok_dolga = S;
        double procent_platezh;
        double osn_dolg_platezh;
        double procent_stavka = ((double)P/12)/100; // (1/12) процентной ставки
        double ezhemesyach_platezh = S * ((procent_stavka *( Math.pow((1 + procent_stavka), period))) / (Math.pow((1 + procent_stavka), period)- 1));

        Calendar calendar = new GregorianCalendar(Integer.parseInt(D.substring(6, 10)), Integer.parseInt(D.substring(3, 5))-1, Integer.parseInt(D.substring(0, 2)));

        String str_json = "{\n";

        for (int i=0; i < N; i++) {
            procent_platezh = ostatok_dolga*procent_stavka;
            osn_dolg_platezh = ezhemesyach_platezh-procent_platezh;
            ostatok_dolga = ostatok_dolga-osn_dolg_platezh;

            str_json = str_json + "   \"platezh_" + i + "\": {\n";
            str_json = str_json + "       \"data_platezha\": \"" + getDate(calendar) + "\",\n";
            str_json = str_json + "       \"ezhemesyach_platezh\": \"" + round(ezhemesyach_platezh, 2) + "\",\n";
            str_json = str_json + "       \"procent_platezh\": \"" + round(procent_platezh, 2) + "\",\n";
            str_json = str_json + "       \"osn_dolg_platezh\": \"" + round(osn_dolg_platezh, 2) + "\",\n";
            str_json = str_json + "       \"ostatok_dolga\": \"" + round(ostatok_dolga, 2) + "\"\n";

            calendar.add(Calendar.MONTH, 1);
            if(i == N-1){str_json = str_json + "   }\n";} else{str_json = str_json + "   },\n";}
        }
        str_json = str_json + "}\n";
        return str_json;
    }

    /*
    Функция расчитывает размер месячного платежа для дифференцированного вида
    *@param S - первоначальная сумма кредита
    *@param P - процентная ставка
    *@param N - количество месяцев
    *@param D - дата первого платежа в формате "dd/MM/yyyy"
    *@return строку в формате json
     */
    public static String func_kalc_dif(int S, int P, int N, String D) {
        // определение переменных
        double period = N;
        double ostatok_dolga = S;
        double procent_platezh;
        double ezhemesyach_platezh;
        double osn_dolg_platezh = ostatok_dolga / period;
        double procent_stavka = ((double)P/12)/100; // (1/12) процентной ставки
        Calendar calendar = new GregorianCalendar(Integer.parseInt(D.substring(6, 10)), Integer.parseInt(D.substring(3, 5))-1, Integer.parseInt(D.substring(0, 2)));
        String str_json = "{\n";

        for (int i=0; i < N; i++) {
            procent_platezh = (S - osn_dolg_platezh * i) * procent_stavka;
            ostatok_dolga = ostatok_dolga - osn_dolg_platezh;
            ezhemesyach_platezh = osn_dolg_platezh + procent_platezh;

            str_json = str_json + "   \"platezh_" + i + "\": {\n";
            str_json = str_json + "       \"data_platezha\": \"" + getDate(calendar) + "\",\n";
            str_json = str_json + "       \"ezhemesyach_platezh\": \"" + round(ezhemesyach_platezh, 2) + "\",\n";
            str_json = str_json + "       \"procent_platezh\": \"" + round(procent_platezh, 2) + "\",\n";
            str_json = str_json + "       \"osn_dolg_platezh\": \"" + round(osn_dolg_platezh, 2) + "\",\n";
            str_json = str_json + "       \"ostatok_dolga\": \"" + round(ostatok_dolga, 2) + "\"\n";

            calendar.add(Calendar.MONTH, 1);
            if(i == N-1){str_json = str_json + "   }\n";} else{str_json = str_json + "   },\n";}
        }
        str_json = str_json + "}\n";
        return str_json;
    }

    /*
    Функция округляет число до N знаков после запятой
    *@param value - десятичное число
    *@param places - количество знаков поосле запятой
    *@return число, округленное до определенного количества знаков после запятой
     */
    private static String round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.toString();
    }

    /*
   Функция преобразовывает дату в строку определённого формата
   *@param calendar - дата
   *@return строку в формате dd/MM/yyyy
   */
    private static String getDate(Calendar calendar) {
        String date;
        int month_int;
        String month_str = null;
        String day_str = null;

        // определение месяца
        month_int = calendar.get(Calendar.MONTH) + 1;
        month_str = ("0" + month_int);
        month_str = month_str.substring(month_str.length()-2);

        // определение дня месяца
        day_str = ("0" + calendar.get(Calendar.DAY_OF_MONTH));
        day_str = day_str.substring(day_str.length()-2);

        date = day_str + "/" + month_str + "/" + calendar.get(Calendar.YEAR);

        return date;
    }

}