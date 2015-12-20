$(document).ready( function () {
    $('#contact-list').DataTable({
        ordering: true,
        searching: true
    });
    $('a.up').remove();
    $('a.down').remove();
});