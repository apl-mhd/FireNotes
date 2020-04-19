package com.example.firenotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firenotes.auth.Register;
import com.example.firenotes.model.Adapter;
import com.example.firenotes.model.Note;
import com.example.firenotes.note.AddNote;
import com.example.firenotes.note.EditNote;
import com.example.firenotes.note.NoteDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteList;
    Adapter adapter;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseFirestore fstore;
    FirebaseUser user;
    FirebaseAuth fAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fstore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();


        Query query = fstore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions <Note> allnotes = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query, Note.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allnotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, final int position, @NonNull final Note model) {


                holder.noteTitle.setText(model.getTitle());
                holder.noteContent.setText(model.getContent());


                final int code =getRandomColor();
                holder.mCardView.setCardBackgroundColor(holder.view.getResources().getColor(code));

                final String docId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent i = new Intent(view.getContext(), NoteDetails.class);

                        i.putExtra("title", model.getTitle());
                        i.putExtra("content", model.getContent());
                        i.putExtra("code",code);
                        i.putExtra("noteId", docId);
                        view.getContext().startActivity(i);


                    }
                });


                ImageView menIcon = holder.view.findViewById(R.id.menuIcon);

                menIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {

                        final  String docId = noteAdapter.getSnapshots().getSnapshot(position).getId();



                        PopupMenu menu = new PopupMenu(view.getContext(), view);
                        //menu.setGravity(Gravity.END);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {


                                Intent i = new Intent(view.getContext(), EditNote.class);
                                i.putExtra("title", model.getTitle());
                                i.putExtra("content", model.getContent());
                                i.putExtra("noteId", docId);
                                startActivity(i);
                                return false;
                            }
                        });

                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {


                                DocumentReference docref = fstore.collection("notes").document(docId);
                                docref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //delete success
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(MainActivity.this, "Error in deleting note", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });

                        menu.show();


                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_laout,parent, false);
                return new NoteViewHolder(view);
            }
        };


        noteList = findViewById(R.id.notelist);

        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close );
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();



        noteList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList.setAdapter(noteAdapter);


        FloatingActionButton floatingActionButton = findViewById(R.id.addNoteFloat);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(view.getContext(), AddNote.class));
            }
        });



    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START);

        Log.i("click", String.valueOf(item.getItemId()));
        Toast.makeText(this, "Orko", Toast.LENGTH_SHORT).show();


        switch (item.getItemId()){

            case R.id.addNote:

                startActivity(new Intent(this, AddNote.class));
                break;

            case R.id.sync:

                if (user.isAnonymous()){

                    startActivity(new Intent(this, Register.class));
                }
                else {

                    Toast.makeText(this, "You are connected", Toast.LENGTH_SHORT).show();
                }


                break;

            case R.id.logout:
                checkUser();
                break;

            default:

                Toast.makeText(this , "Coming soon", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    private void checkUser() { //if user anonymous or not


        if (user.isAnonymous()){

            displayAlert();
        }
        else {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Splash.class));
                finish();
        }
    }

    private void displayAlert() {

        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are yoy sure?")
                .setMessage("You are logged in with temporary Account. Logging out will delete all notes")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();

                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //delete all dataa


                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                startActivity(new Intent(getApplicationContext(), Splash.class));
                                finish();
                            }
                        });
                    }
                });

        warning.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.settings){

            Toast.makeText(MainActivity.this, "Setting menu is Sellected", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public  class NoteViewHolder extends  RecyclerView.ViewHolder{

        TextView noteTitle, noteContent;
        View view;
        CardView mCardView;


        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            mCardView = itemView.findViewById(R.id.noteCard);

            view  = itemView;

        }
    }

    private int getRandomColor(){


        List<Integer> colorCode = new ArrayList<>();

        colorCode.add(R.color.blue);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.skyBlue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.gray);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.red);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.notGreen);

        Random randomColor = new Random();

        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);

    }


    @Override
    protected void onStart() {
        super.onStart();

        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.startListening();
    }
}
