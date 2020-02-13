import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    /**
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

// запускаем подключение сокета по известным координатам и нициализируем приём сообщений с консоли клиента
        try(Socket socket = new Socket("localhost", 3345);
            BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
            DataInputStream ois = new DataInputStream(socket.getInputStream());    )
        {
            System.out.println("Client connected to socket.");
            String [] countryar = read("export.csv");

// проверяем живой ли канал и работаем
            for (int i = 0; !socket.isOutputShutdown(); i++) {

                System.out.println("Client start writing in channel...");
                Thread.sleep(1000);
                String clientCommand = countryar[i];
                oos.writeUTF(clientCommand);
                oos.flush();
                System.out.println("Clien sent message " + clientCommand + " to server.");
                Thread.sleep(1000);
// ждём чтобы сервер успел прочесть сообщение из сокета и ответить

// проверяем условие выхода из соединения
                if(clientCommand.equalsIgnoreCase("quit")){

// если условие выхода достигнуто разъединяемся
                    System.out.println("Client kill connections");
                    Thread.sleep(2000);

// смотрим что нам ответил сервер на последок
                    if(ois.available()!=0)    {
                        System.out.println("reading...");
                        String in = ois.readUTF();
                        System.out.println(in);
                    }
// после предварительных приготовлений выходим из цикла записи чтения
                    break;
                }

// если условие разъединения не достигнуто продолжаем работу
                System.out.println("Client wrote & start waiting for data from server...");
                Thread.sleep(2000);

// проверяем, что нам ответит сервер на сообщение(за предоставленное ему время в паузе он должен был успеть ответить
                if(ois.available()!=0)    {

// если успел забираем ответ из канала сервера в сокете и сохраняемеё в ois переменную,  печатаем на консоль
                    System.out.println("reading...");
                    String in = ois.readUTF();
                    System.out.println(in);
                }
            }

            System.out.println("Closing connections & channels on clentSide - DONE.");

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
    Функция читает csv-файл и возращает массив запросов
    *@param csvFile - путь к файлу и название файла
    *@return массив запросов
     */
    private static String[] read(String csvFile) {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        String[] country;
        Boolean firststr = false; // учитывать первую строку
        String[] countryarr = new String[5];

        try {

            br = new BufferedReader(new FileReader(csvFile));
            int j=0;
            while ((line = br.readLine()) != null) {
                if(firststr==false) {
                    firststr=true;
                } else {
                    // use comma as separator
                    country = line.split(cvsSplitBy);
                    //System.out.println("GET?param1=" + country[0] + "&param2=" + country[1] + "&param3=" + country[2] + "&payment_type=" + country[3] + "&param5=" + country[4] + "&param6=" + country[5]);

                    String request = "GET?amount_of_credit=" + country[0] + // размер кредита
                            "&interest_of_credit=" + country[1] +           // процент по кредиту
                            "&amount_of_first_payment=" + country[2] +      // размер первоначального взноса
                            "&type_of_payment=" + country[3] +              // вид платежей (annuity или differential)
                            "&period=" + country[4] +                       // срок кредита
                            "&date_of_first_payment=" + country[5];         // срок первого платежа
                    countryarr[j]=request;
                    j++;
                }
            }
            countryarr[j] = "quit";

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return countryarr;
    }
}