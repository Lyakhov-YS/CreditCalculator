import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws InterruptedException {

// запускаем подключение сокета по известным координатам и нициализируем приём сообщений с консоли клиента
        try(Socket socket = new Socket("localhost", 3345);
            BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
            DataInputStream ois = new DataInputStream(socket.getInputStream());    )
        {

            System.out.println("Client connected to socket.");
            System.out.println();
            System.out.println("Client writing channel = oos & reading channel = ois initialized.");

            String [] countryar = run();
            int i = 0;
// проверяем живой ли канал и работаем если тру
            while(!socket.isOutputShutdown()){

// ждём консоли клиента на предмет появления в ней данных
                //if(br.ready()){

                for (; countryar[i]!="Done"; i++) {

// данные появились - работаем
                    System.out.println("Client start writing in channel...");
                    Thread.sleep(1000);
                    //String clientCommand = br.readLine();
                    String clientCommand = countryar[i];

// пишем данные с консоли в канал сокета для сервера
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

    public static String[] run() {

        String csvFile = "test.csv";
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
                    //System.out.println("GET?param1=" + country[0] + "&param2=" + country[1] + "&param3=" + country[2] + "&param4=" + country[3] + "&param5=" + country[4] + "&param6=" + country[5]);

                    String request = "GET?";
                    for (int i = 0; i <= 5; i++) {
                        if (i != 0) {
                            request = request + "&";
                        }
                        request = request + "param" + i + "=" + country[i];
                    }
                    countryarr[j]=request;
                    j++;
                    //System.out.println(request);
                }
            }




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
        countryarr[4]="Done";
        //System.out.println("Done");
        return countryarr;
    }
}