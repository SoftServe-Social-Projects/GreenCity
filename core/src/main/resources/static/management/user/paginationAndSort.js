function Order() {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var url = "/management/users?";


    var items = localStorage.getItem('size');
    if(items==null){
        items = "20";
    }
    url = url + "size=" + items.toString();

    var sort = localStorage.getItem('sort');
    if(sort==null){
        sort="id,ASC";
    }

    url = url + '&sort=' + sort.toString();
    window.location.href = url;


}