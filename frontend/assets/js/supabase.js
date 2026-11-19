(() => {
  const status = document.querySelector("#supabaseStatus");
  const { supabaseUrl, supabaseAnonKey } = window.LEDGERLY_CONFIG || {};
  if (!window.supabase || !supabaseUrl || !supabaseAnonKey) {
    status.innerHTML = '<i class="status-dot"></i> Supabase setup needed';
    status.title = "Add the publishable/anon key in assets/js/config.js to complete the connection.";
    return;
  }
  try {
    window.ledgerlySupabase = window.supabase.createClient(supabaseUrl, supabaseAnonKey, {
      auth: { autoRefreshToken: true, persistSession: true, detectSessionInUrl: true }
    });
    status.innerHTML = '<i class="status-dot"></i> Connecting to Supabase';
    fetch(`${supabaseUrl}/auth/v1/health`, { headers: { apikey: supabaseAnonKey } })
      .then(response => {
        if (!response.ok) throw new Error(`Supabase returned HTTP ${response.status}`);
        status.innerHTML = '<i class="status-dot" style="background:#36a879"></i> Supabase connected';
        status.title = "Supabase Auth endpoint is reachable.";
      })
      .catch(error => {
        status.innerHTML = '<i class="status-dot" style="background:#d46a6a"></i> Supabase unavailable';
        status.title = error.message;
      });
  } catch (error) {
    status.innerHTML = '<i class="status-dot" style="background:#d46a6a"></i> Supabase setup issue';
    status.title = error.message;
  }
})();
