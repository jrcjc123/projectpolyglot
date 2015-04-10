/*
@author Dean Chen
 */
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;


import java.io.*;
import java.util.*;

/*
Homework

Save data extracted from movie.widget.html file (see beginning of the file for the file format description) into a .txt file in the following format:

Total movies: total_number_of_movies
Total genres: total_number_of_genres
GENRE
GENRE
...
=== No genre ===
MOVIE
MOVIE
...

Where GENRE contains information about the genre and all movies attached to this genre (value in [] may not exist):

Genre: genre_id[, max_popularity_within_genre]
MOVIE
MOVIE
...

and MOVIE contains information about the movie (values in [] may not exist):

Movie: movie_title[ (release_year)][, popularity]

Genres must be sorted by popularity (descending, i.e. genre with the highest value goes first) and then by genre id.
Movies must be sorted by popularity (descending, i.e. movie with the highest value goes first) and then by movie title that includes release year,
if this information is present. Only popularity greater and equal than 0 must be written into the file. An example of the output file is attached.
*/



public class MovieStatisticsGenerator {

    public static final String MOVIE = "movie";
    public static final String DATA_RL = "data-rl";
    public static final String DATA_G = "data-g";
    public static final String DATA_POP = "data-pop";
    public static final String DATA_TYPE = "data-type";
    public static final String DATA_T = "data-t";
    private final String inputFileName = "movie.widget.html";
    private final String outputFileName = "statistics.txt";
    private final List<Movie> noGenreMovies = new ArrayList<Movie>();

    private String inputFileLocation;
    private String outputFileLocation;
    private Integer totalMovieCount = 0;  //convenience counter

    private SortedMap<String, Genre> genres = new TreeMap<String, Genre>();

    //helper data structure to sort genre map, sort by value instead of key
    private SortedSet<Map.Entry<String, Genre>> genresSet = new TreeSet<Map.Entry<String, Genre>>(
            new Comparator<Map.Entry<String, Genre>>() {
                @Override
                public int compare(Map.Entry<String, Genre> e1,
                                   Map.Entry<String, Genre> e2) {
                    return e2.getValue().maxPopularity - e1.getValue().maxPopularity;
                }
            });

    //Models
    private static class Genre {
        private String genreId;
        private Integer maxPopularity=-1;
        private List<Movie> movies;

        @Override
        public String toString() {
            return "Genre{" +
                    "genreId='" + genreId + '\'' +
                    ", maxPopularity=" + maxPopularity +
                    '}';
        }
    }
    private static class Movie {
        private String title;
        private Integer releaseYear=-1;
        private Integer popularity=-1;
        private String genreId;

        @Override
        public String toString() {
            return "Movie{" +
                    "title='" + title + '\'' +
                    ", releaseYear=" + releaseYear +
                    ", popularity=" + popularity +
                    ", genreId='" + genreId + '\'' +
                    '}';
        }
    }

    private static class MovieComparator implements Comparator<Movie> {

        @Override
        public int compare(Movie o1, Movie o2) {
            // "Movies must be sorted by popularity (descending, i.e. movie with the highest value goes first) and"
            //
            int result = o2.popularity - o1.popularity;

            if(result != 0)
                return result;
            else {
                //"then by movie title that includes release year, if this information is present"
                if(o1.releaseYear > -1 && o2.releaseYear > -1)
                    return o1.title.compareTo(o2.title);
                else
                    //interpreting the instruction, if no release year, then it is at the end.
                    return o2.releaseYear - o1.releaseYear;

            }
        }
    }


    //initialize the data
    private boolean init() {

        File dir = new File(".");
        Reader reader = null;

        try {
            inputFileLocation = dir.getCanonicalPath() + File.separator + inputFileName;
            outputFileLocation = dir.getCanonicalPath() + File.separator + outputFileName;


            FileInputStream fis = new FileInputStream(new File(inputFileLocation));
            // //Construct the BufferedReader object
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            StringWriter sw = new StringWriter();
            String line;

            while ((line = in.readLine()) != null)
            {
                sw.append(line);
            }

            reader = new StringReader(sw.getBuffer().toString());

            HTMLEditorKit.Parser parser = new ParserDelegator();
            parser.parse(reader, new HTMLEditorKit.ParserCallback()
            {
                public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
                {
                    if (t == HTML.Tag.A)
                    {
                        Object dataType = a.getAttribute(DATA_TYPE);
                        if(dataType!=null && ((String)dataType).equalsIgnoreCase(MOVIE)) {


                            Object title = a.getAttribute(DATA_T);
                            Movie movie = new Movie();
                            if (title != null) {
                                movie.title = title.toString();
                            }
                            Object releaseYear = a.getAttribute(DATA_RL);
                            if (releaseYear != null) {
                                movie.releaseYear = Integer.parseInt(releaseYear.toString());
                            }

                            Object popularity = a.getAttribute(DATA_POP);
                            if (popularity != null) {
                                movie.popularity = Integer.parseInt(popularity.toString());
                            }


                            Object genreId = a.getAttribute(DATA_G);
                            if (genreId != null) {
                                movie.genreId = genreId.toString();

                                Genre genre = null;

                                if(!genres.containsKey(movie.genreId)) {
                                    genre = new Genre();
                                    genre.genreId = movie.genreId;
                                    genre.maxPopularity = movie.popularity;
                                    genre.movies = new ArrayList<Movie>();
                                    genre.movies.add(movie);
                                    genres.put(movie.genreId, genre);
                                }
                                else {
                                    genre = genres.get(movie.genreId);
                                    int maxPouplarity = genre.maxPopularity;
                                    if(maxPouplarity < movie.popularity) {
                                        //this will update the reference object's field
                                        //ok for single threaded.
                                        genre.maxPopularity = movie.popularity;
                                    }
                                    genre.movies.add(movie);

                                }

                            }else {
                                //no genre movies
                                noGenreMovies.add(movie);
                            }


                            totalMovieCount++;
                        }
                    }
                }
            }, true);

            //helper data structure for sorting genres
            genresSet.addAll(genres.entrySet());

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        } finally {
            if(reader != null) try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        return true;

    }

    private boolean extractMovieDataForPrint() {

        BufferedWriter bw = null;

        try {
            File fout = new File(outputFileLocation);
            FileOutputStream fos = null;

            fos = new FileOutputStream(fout);

            bw = new BufferedWriter(new OutputStreamWriter(fos));

            bw.write("Total movies: " + totalMovieCount);
            bw.newLine();

            bw.write("Total genres: " + genresSet.size());
            bw.newLine();


            if(genresSet.size() > 0 ) {

                Iterator<Map.Entry<String, Genre>> itr = genresSet.iterator();
                while (itr.hasNext()) {
                    Map.Entry<String, Genre> entries = itr.next();
                    String genreId = entries.getKey();
                    Genre genre = entries.getValue();
                    bw.newLine();
                    bw.write("Genre: " + genreId + ", " + (genre.maxPopularity > -1? genre.maxPopularity: 0));
                    bw.newLine();
                    prettyPrintMovieToFile(bw, genre.movies);
                }

            }

            if(noGenreMovies.size() > 0) {

                bw.newLine();
                bw.write("=== No genre ===");
                bw.newLine();
                prettyPrintMovieToFile(bw, noGenreMovies);
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(bw != null) try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        return true;

    }

    private void prettyPrintMovieToFile(BufferedWriter bw, List<Movie> movies) throws IOException {

        Collections.sort(movies, new MovieComparator());
        for(Movie m : movies){
            bw.write("Movie: " + m.title + (m.releaseYear!=-1 ? " (" + m.releaseYear + ")" :"") +  (m.popularity!=-1 ?  ", " + m.popularity :""));
            bw.newLine();
        }
    }


    public static void main(String[] args) {

        MovieStatisticsGenerator msg = new MovieStatisticsGenerator();
        if(msg.init()) {
            if(msg.extractMovieDataForPrint())
                System.out.println("Create Statistics Complete");
            else
                System.err.println("Error create statistics.");
        }
        else
            System.err.println("Error create input.");


    }
}
