package pangtourista.project.Fragments;

import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import pangtourista.project.R;

public class OrdersFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private DrawerLayout drawerLayout;
    private ImageView navigationBar, iv_logout;
    private LinearLayout ll_First, ll_Second, ll_Third, ll_Fourth, ll_Fifth, ll_Sixth, ll_Seventh;
    private NavigationView navigationView;
    private TextView tv_logout;

    public OrdersFragment() {
        // Required empty public constructor
    }

    public static OrdersFragment newInstance(String param1, String param2) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        onSetNavigationDrawerEvents(view);
        return view;
    }

    private void onSetNavigationDrawerEvents(View view) {
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);

        drawerLayout.openDrawer(GravityCompat.START);

        navigationBar = view.findViewById(R.id.navigationBar);


        iv_logout = view.findViewById(R.id.iv_logout);
        tv_logout = view.findViewById(R.id.tv_logout);

        navigationBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "navigationBar", Toast.LENGTH_SHORT).show();
            }
        });

        ll_First.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "First", Toast.LENGTH_SHORT).show();
            }
        });

        ll_Second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "Second", Toast.LENGTH_SHORT).show();
            }
        });

        ll_Third.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "Third", Toast.LENGTH_SHORT).show();
            }
        });

        ll_Fourth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "Fourth", Toast.LENGTH_SHORT).show();
            }
        });

        ll_Fifth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "Fifth", Toast.LENGTH_SHORT).show();
            }
        });

        ll_Sixth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "Sixth", Toast.LENGTH_SHORT).show();
            }
        });

        ll_Seventh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
                Toast.makeText(getContext(), "Seventh", Toast.LENGTH_SHORT).show();
            }
        });

        iv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
            }
        });

        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView, true);
            }
        });

    }
}
