import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client implements Runnable{
    @Override
    public void run() {
        int serverPort = 8080;
        String address = "127.0.0.1";
        SocketAddress remote = new InetSocketAddress(address, serverPort);
        int count = 0;
        String [] countryar = read("export.csv");
        while (!countryar[count].equals("quit")) {
            //while (count<2) {
            try {
                // отправка сообщения
                String msg = countryar[count];
                byte[] bt = msg.getBytes("UTF-8");
                ByteBuffer buffer = ByteBuffer.wrap(bt);
                SocketChannel outChannel = SocketChannel.open(remote);
                System.out.println("Устанавливаю контакт!");
                outChannel.write(buffer);
                System.out.println("Отправляю сообщение!");

                //Thread.sleep(100);

                // приём сообщения
                buffer = ByteBuffer.allocate(100000);
                int byteRead = outChannel.read(buffer);
                String st = new String(buffer.array(), "UTF-8");
                System.out.println("Получено сообщение: " + st);

                count = count + 1;
            } catch (Exception er) {
                er.printStackTrace();
            }
        }

    }

    public static void main(String[] ar) {
        Client client = new Client();
        Thread th = new Thread(client);
        th.start();
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
        String[] countryarr = new String[1000];

        try {

            br = new BufferedReader(new FileReader(csvFile));
            int j=0;
            while ((line = br.readLine()) != null) {
                if(firststr==false) {
                    firststr=true;
                } else {
                    // use comma as separator
                    country = line.split(cvsSplitBy);
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
